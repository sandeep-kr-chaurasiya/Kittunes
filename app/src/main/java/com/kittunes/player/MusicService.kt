package com.kittunes.player

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.kittunes.api_data.Data

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
        // Check if the current song is the same as the new song
        if (currentSong != song) {
            releaseMediaPlayer() // Release existing player
            currentSong = song
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(song.preview) // Ensure 'preview' is the correct field
                    setOnPreparedListener {
                        if (isPlaying) {
                            start() // Start if already playing
                        }
                    }
                    setOnCompletionListener {
                        currentSong = null
                        this@MusicService.currentPosition = 0
                        // Optionally handle moving to the next song here
                    }
                    prepareAsync() // Prepare asynchronously
                } catch (e: Exception) {
                    Log.e("MusicService", "Error initializing MediaPlayer", e)
                    stopPlayback()
                }
            }
        } else {
            // Restore position if the same song is prepared again
            mediaPlayer?.seekTo(currentPosition)
        }
    }

    fun startPlayback() {
        mediaPlayer?.takeIf { !it.isPlaying }?.start()
    }

    fun pausePlayback() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
        currentPosition = mediaPlayer?.currentPosition ?: 0 // Save current position
    }

    fun resumePlayback() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.seekTo(currentPosition)
                it.start()
            }
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

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }
}