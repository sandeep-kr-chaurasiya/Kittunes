package com.kittunes.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.kittunes.Api_Data.Data

class MusicService : Service() {

    var mediaPlayer: MediaPlayer? = null
    var currentSong: Data? = null
    var currentPosition: Int = 0

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
            mediaPlayer?.seekTo(currentPosition)
            mediaPlayer?.start()
        } else {
            releaseMediaPlayer()
            currentSong = song
            mediaPlayer = MediaPlayer().apply {
                setDataSource(song.preview)
                setOnPreparedListener {
                    start()
                    this@MusicService.currentPosition = 0
                }
                setOnCompletionListener {
                    currentSong = null
                    this@MusicService.currentPosition = 0
                }
                prepareAsync()
            }
        }
    }

    fun pausePlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                currentPosition = it.currentPosition
                it.pause()
            }
        }
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
        mediaPlayer?.release()
        mediaPlayer = null
        currentSong = null
        currentPosition = 0
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }
}