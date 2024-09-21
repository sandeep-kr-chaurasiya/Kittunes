package com.kittunes.player

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.gson.Gson
import com.kittunes.Api_Data.Data
import com.kittunes.Api_Data.Playlist

class SharedViewModel(context: Context) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private val _songList = MutableLiveData<MutableList<Data>>(mutableListOf())
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
        loadQueueFromPreferences()
        fetchPlaylists()
    }

    fun setCurrentSong(song: Data?) {
        _currentSong.value = song
        song?.let { saveSongToPreferences(it) }
    }

    fun addSongToQueue(song: Data) {
        val currentList = _songList.value ?: mutableListOf()
        if (!currentList.contains(song)) {
            currentList.add(song)
            _songList.value = currentList
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

        val playlistRef = firestore.collection("Playlists").document(userId).collection("UserPlaylists").document(playlistId)

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

    private fun saveSongToPreferences(song: Data) {
        prefs.edit().apply {
            putString("currentSong", gson.toJson(song))
            apply()
        }
    }

    private fun getStoredSong(): Data? {
        return prefs.getString("currentSong", null)?.let {
            gson.fromJson(it, Data::class.java)
        }
    }

    private fun savePlayingStateToPreferences(isPlaying: Boolean) {
        prefs.edit().apply {
            putBoolean("isPlaying", isPlaying)
            apply()
        }
    }

    private fun loadQueueFromPreferences() {
        // Load the queue from SharedPreferences and update _songList
        // Implement as needed
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

        val playlistRef = firestore.collection("Playlists").document(userId).collection("UserPlaylists").document(playlistId)
        playlistRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val songs = document.get("songs") as? List<Map<String, Any>> ?: emptyList()
                Log.d(TAG, "Fetched songs: $songs")  // Log the raw data

                if (songs.isNotEmpty()) {
                    // Convert Map to Data object
                    val songDataList = songs.mapNotNull { map ->
                        try {
                            // Convert Map to Data object
                            val jsonString = Gson().toJson(map)
                            Gson().fromJson(jsonString, Data::class.java)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error converting map to Data", e)
                            null
                        }
                    }
                    // Post the songs data to LiveData
                    _playlistSongs.postValue(songDataList)
                } else {
                    Log.e(TAG, "No songs found in the playlist")
                    _playlistSongs.postValue(emptyList())  // Ensure empty list is posted
                }
            } else {
                Log.e(TAG, "Playlist does not exist with ID: $playlistId")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error fetching songs from playlist", e)
        }
    }

    companion object {
        private const val TAG = "SharedViewModel"
    }
}