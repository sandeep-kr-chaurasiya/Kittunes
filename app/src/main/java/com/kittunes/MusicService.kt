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
    private var sharedViewModel: SharedViewModel? = null

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false

    private val binder = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun playSong(song: Data) {
        if (currentSong == song && mediaPlayer != null) {
            mediaPlayer?.apply {
                seekTo(currentPosition)
                start()
            }
        } else {
            releaseMediaPlayer()
            currentSong = song
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(song.preview)
                    setOnPreparedListener {
                        start()
                        this@MusicService.currentPosition = 0
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
        }
    }

    private fun onSongComplete() {
        sharedViewModel?.playNextSong()
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

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }
}