package com.kittunes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kittunes.Api_Data.Data

class SharedViewModel : ViewModel() {

    private val _currentSong = MutableLiveData<Data?>()
    val currentSong: LiveData<Data?> get() = _currentSong

    private val _currentPosition = MutableLiveData<Int>().apply { value = 0 }  // Initialize with 0
    val currentPosition: LiveData<Int> get() = _currentPosition

    private val _isPlaying = MutableLiveData<Boolean>().apply { value = false }  // Initialize with false
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    fun setCurrentSong(song: Data?) {
        _currentSong.value = song
    }

    fun setCurrentPosition(position: Int) {
        _currentPosition.value = position
    }

    // Set play/pause state
    fun setPlayingState(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    fun updateCurrentPosition(position: Int) {
        if (_currentPosition.value != position) {
            _currentPosition.value = position
        }
    }
}