package com.kittunes.player

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.gson.Gson
import com.kittunes.Api_Data.Data
import com.kittunes.Api_Data.Playlist

class SharedViewModel(context: Context) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val firestore = FirebaseFirestore.getInstance()
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

    init {
        // Load the song queue and playlists from local storage or Firestore
        loadQueueFromPreferences()
        fetchPlaylists()
    }

    fun setCurrentSong(song: Data?) {
        _currentSong.value = song
        if (song != null) {
            saveSongToPreferences(song)
        }
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
        val list = _songList.value ?: return
        val currentIndex = list.indexOf(_currentSong.value)
        if (currentIndex != -1 && currentIndex < list.size - 1) {
            val nextSong = list[currentIndex + 1]
            setCurrentSong(nextSong)
        }
    }

    fun playPreviousSong() {
        val list = _songList.value ?: return
        val currentIndex = list.indexOf(_currentSong.value)
        if (currentIndex > 0) {
            val previousSong = list[currentIndex - 1]
            setCurrentSong(previousSong)
        }
    }

    fun addSongToPlaylist(playlist: Playlist, song: Data) {
        val playlistId = playlist.playlistId ?: return
        val playlistRef = firestore.collection("Playlists").document(playlistId)

        playlistRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Document exists, update it
                playlistRef.update("songs", FieldValue.arrayUnion(song))
                    .addOnSuccessListener {
                        Log.d(TAG, "Song added to playlist successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to add song to playlist", e)
                    }
            } else {
                // Document does not exist, create it
                playlistRef.set(mapOf("songs" to listOf(song)))
                    .addOnSuccessListener {
                        Log.d(TAG, "Playlist created and song added successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to create playlist and add song", e)
                    }
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error fetching playlist document", e)
        }
    }

    private fun saveSongToPreferences(song: Data) {
        val editor = prefs.edit()
        editor.putString("currentSong", gson.toJson(song))
        editor.apply()
    }

    private fun getStoredSong(): Data? {
        val songJson = prefs.getString("currentSong", null) ?: return null
        return gson.fromJson(songJson, Data::class.java)
    }

    private fun savePlayingStateToPreferences(isPlaying: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean("isPlaying", isPlaying)
        editor.apply()
    }

    private fun loadQueueFromPreferences() {
        // Load the queue from SharedPreferences and update _songList
    }

    private fun fetchPlaylists() {
        if (userId != null) {
            val userPlaylistsRef = firestore.collection("Playlists").document(userId).collection("UserPlaylists")

            userPlaylistsRef.get()
                .addOnSuccessListener { result ->
                    val playlists = mutableListOf<Playlist>()
                    for (document in result) {
                        val playlist = document.toObject(Playlist::class.java)
                        playlists.add(playlist)
                    }
                    _playlists.value = playlists
                }
                .addOnFailureListener { e ->
                    // Handle the error
                    Log.e(TAG, "Error fetching playlists: ${e.message}", e)
                }
        } else {
            Log.e(TAG, "Error: User not logged in")
        }
    }

    companion object {
        private const val TAG = "SharedViewModel"
    }
}