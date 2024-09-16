package com.kittunes.player

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
        // Ensure that this method does not restart playback
        if (currentSong != song) {
            releaseMediaPlayer()
            currentSong = song
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(song.preview)
                    setOnPreparedListener {
                        if (isPlaying) {
                            start()
                        }
                    }
                    setOnCompletionListener {
                        currentSong = null
                        this@MusicService.currentPosition = 0
                    }
                    prepareAsync()
                } catch (e: Exception) {
                    Log.e("MusicService", "Error initializing MediaPlayer", e)
                    stopPlayback()
                }
            }
        }
    }

    fun startPlayback() {
        mediaPlayer?.takeIf { !it.isPlaying }?.start()
    }

    fun pausePlayback() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
        // Save the current position when paused
        currentPosition = mediaPlayer?.currentPosition ?: 0
    }

    fun resumePlayback() {
        mediaPlayer?.takeIf { !it.isPlaying }?.start()
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