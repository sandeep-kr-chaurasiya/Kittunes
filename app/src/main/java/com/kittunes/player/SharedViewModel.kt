package com.kittunes.player

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.kittunes.Api_Data.Data
import com.kittunes.Api_Data.Playlist
import com.kittunes.main.MainActivity

class SharedViewModel(context: Context) : ViewModel() {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    @SuppressLint("StaticFieldLeak")
    private var musicService: MusicService? = null

    fun setMusicService(service: MusicService) {
        musicService = service
    }

    private val _songList = MutableLiveData<MutableList<Data>>(mutableListOf()).apply {
        value = loadSongListFromPreferences()  // Load the saved song list from preferences
    }
    val songList: LiveData<MutableList<Data>> get() = _songList

    private val _currentSong = MutableLiveData<Data?>().apply {
        value = getStoredSong()
    }
    val currentSong: LiveData<Data?> get() = _currentSong

    private val _currentPosition = MutableLiveData<Int>().apply {
        value = prefs.getInt("currentPosition", 0)
    }
    val currentPosition: LiveData<Int> get() = _currentPosition

    private val _isPlaying = MutableLiveData<Boolean>().apply {
        value = prefs.getBoolean("isPlaying", false)
    }
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> get() = _playlists

    private val _playlistSongs = MutableLiveData<List<Data>>()
    val playlistSongs: LiveData<List<Data>> get() = _playlistSongs

    init {
        fetchPlaylists()
    }

    fun onSongClicked(song: Data, activity: MainActivity, isBound: Boolean) {
        addSongToQueue(song)
        setCurrentSong(song)
        setPlayingState(true)

        if (isBound) {
            musicService?.prepareSong(song)
            musicService?.mediaPlayer?.setOnPreparedListener {
                musicService?.startPlayback()
            }
        } else {
            val serviceIntent = Intent(activity, MusicService::class.java)
            activity.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        activity.binding.currentsong.visibility = View.VISIBLE
    }

    fun setCurrentSong(song: Data?) {
        _currentSong.value = song
        song?.let { saveSongToPreferences(it) }
    }

    // Update to add new song at the beginning of the list
    fun addSongToQueue(song: Data) {
        val currentList = _songList.value ?: mutableListOf()
        if (!currentList.contains(song)) {
            currentList.add(0, song) // Add the new song at the beginning
            _songList.value = currentList
            saveSongListToPreferences(currentList)  // Save the updated song list
        }
    }

    fun setPlayingState(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
        savePlayingStateToPreferences(isPlaying)
    }

    fun playNextSong() {
        _songList.value?.let { list ->
            val currentIndex = list.indexOf(_currentSong.value)
            if (currentIndex in 0 until list.size - 1) {
                setCurrentSong(list[currentIndex + 1])
            }
        }
    }

    fun playPreviousSong() {
        _songList.value?.let { list ->
            val currentIndex = list.indexOf(_currentSong.value)
            if (currentIndex > 0) {
                setCurrentSong(list[currentIndex - 1])
            }
        }
    }

    fun addSongToPlaylist(playlistId: String, song: Data) {
        if (userId == null) {
            Log.e(TAG, "Error: User not logged in")
            return
        }

        val playlistRef =
            firestore.collection("Playlists").document(userId).collection("UserPlaylists")
                .document(playlistId)

        playlistRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                playlistRef.update("songs", FieldValue.arrayUnion(song))
                    .addOnSuccessListener {
                        Log.d(TAG, "Song added to playlist successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to add song to playlist", e)
                    }
            } else {
                Log.e(TAG, "Playlist does not exist with ID: $playlistId")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error fetching playlist document", e)
        }
    }

    fun fetchPlaylists() {
        if (userId != null) {
            firestore.collection("Playlists").document(userId).collection("UserPlaylists")
                .get()
                .addOnSuccessListener { result ->
                    val playlists = result.mapNotNull { document ->
                        document.toObject(Playlist::class.java)
                    }
                    _playlists.value = playlists
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error fetching playlists", e)
                }
        } else {
            Log.e(TAG, "Error: User not logged in")
        }
    }

    fun fetchSongsFromPlaylist(playlistId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "Error: User not logged in")
            return
        }

        val playlistRef =
            firestore.collection("Playlists").document(userId).collection("UserPlaylists")
                .document(playlistId)
        playlistRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val songs = document.get("songs") as? List<Map<String, Any>> ?: emptyList()
                Log.d(TAG, "Fetched songs: $songs")

                if (songs.isNotEmpty()) {
                    val songDataList = songs.mapNotNull { map ->
                        try {
                            val jsonString = Gson().toJson(map)
                            Gson().fromJson(jsonString, Data::class.java)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error converting map to Data", e)
                            null
                        }
                    }
                    _playlistSongs.postValue(songDataList)
                } else {
                    Log.e(TAG, "No songs found in the playlist")
                    _playlistSongs.postValue(emptyList())
                }
            } else {
                Log.e(TAG, "Playlist does not exist with ID: $playlistId")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error fetching songs from playlist", e)
        }
    }

    // Start playback
    fun startPlayback() {
        musicService?.startPlayback()
        setPlayingState(true)
    }

    // Save the current song to SharedPreferences
    private fun saveSongToPreferences(song: Data) {
        prefs.edit().apply {
            putString("currentSong", gson.toJson(song))
            apply()
        }
    }

    // Get the stored song from SharedPreferences
    private fun getStoredSong(): Data? {
        return prefs.getString("currentSong", null)?.let {
            gson.fromJson(it, Data::class.java)
        }
    }

    // Save the playing state to SharedPreferences
    private fun savePlayingStateToPreferences(isPlaying: Boolean) {
        prefs.edit().apply {
            putBoolean("isPlaying", isPlaying)
            apply()
        }
    }

    // Save the song list to SharedPreferences
    private fun saveSongListToPreferences(songList: List<Data>) {
        val jsonString = gson.toJson(songList)
        prefs.edit().apply {
            putString("songList", jsonString)
            apply()
        }
    }

    // Load the song list from SharedPreferences
    private fun loadSongListFromPreferences(): MutableList<Data> {
        val jsonString = prefs.getString("songList", null)
        return if (jsonString != null) {
            val type = object : com.google.gson.reflect.TypeToken<MutableList<Data>>() {}.type
            gson.fromJson<MutableList<Data>>(jsonString, type)
        } else {
            mutableListOf()
        }
    }
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            // Optional: Start playback immediately if needed
            if (isPlaying.value == true) {
                musicService?.startPlayback()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
        }
    }
    companion object {
        private const val TAG = "SharedViewModel"
    }
}