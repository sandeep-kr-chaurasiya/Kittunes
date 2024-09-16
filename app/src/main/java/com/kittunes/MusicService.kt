package com.kittunes

import SharedViewModel
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.kittunes.Api_Data.Data

class MusicService : Service() {

    var mediaPlayer: MediaPlayer? = null
    var currentSong: Data? = null
    private var currentPosition: Int = 0
    private val binder = MusicBinder()

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun prepareSong(song: Data) {
        if (currentSong != song) {
            releaseMediaPlayer()
            currentSong = song
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(song.preview)
                    setOnPreparedListener {
                        // Set the initial position to 0
                        this@MusicService.currentPosition = 0
                        // Prepare the song without starting playback
                    }
                    setOnCompletionListener {
                        onSongComplete()
                    }
                    prepareAsync()
                } catch (e: Exception) {
                    Log.e("MusicService", "Error initializing MediaPlayer", e)
                    stopPlayback()
                }
            }
        } else {
            // If the same song is selected, just seek to the last position
            mediaPlayer?.seekTo(currentPosition)
        }
    }

    fun startPlayback() {
        mediaPlayer?.takeIf { !it.isPlaying }?.apply {
            seekTo(currentPosition)
            start()
        }
    }

    fun pausePlayback() {
        mediaPlayer?.takeIf { it.isPlaying }?.apply {
            this@MusicService.currentPosition = currentPosition
            pause()
        }
    }

    fun resumePlayback() {
        mediaPlayer?.takeIf { !it.isPlaying }?.apply {
            seekTo(currentPosition)
            start()
        }
    }

    fun stopPlayback() {
        releaseMediaPlayer()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.apply {
            release()
            mediaPlayer = null
        }
        currentSong = null
        currentPosition = 0
    }

    private fun onSongComplete() {
        // Handle what happens when the song is complete
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }
}