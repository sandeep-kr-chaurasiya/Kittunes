package com.kittunes.fragments

import SharedViewModel
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kittunes.Api_Data.Data
import com.kittunes.R
import com.kittunes.databinding.SongDetailBottomBinding
import com.kittunes.services.MusicService

class SongDetailBottomFragment : BottomSheetDialogFragment() {

    private var _binding: SongDetailBottomBinding? = null
    private val binding get() = _binding!!

    private var song: Data? = null
    private var autoPlay = false
    private var musicService: MusicService? = null
    private var isBound = false
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val handler = Handler()
    private val updateSeekBarTask = object : Runnable {
        override fun run() {
            if (isBound && musicService?.mediaPlayer != null) {
                val currentPosition = musicService!!.mediaPlayer!!.currentPosition
                val duration = musicService!!.mediaPlayer!!.duration
                binding.seekBar.max = duration
                binding.seekBar.progress = currentPosition
                binding.currentTime.text = formatTime(currentPosition)
                binding.duration.text = formatTime(duration)
                handler.postDelayed(this, 1000)  // Schedule update every second
            }
        }
    }

    companion object {
        private const val ARG_SONG = "arg_song"
        private const val ARG_AUTO_PLAY = "arg_auto_play"

        fun newInstance(song: Data, autoPlay: Boolean): SongDetailBottomFragment {
            val fragment = SongDetailBottomFragment()
            val args = Bundle().apply {
                putParcelable(ARG_SONG, song)
                putBoolean(ARG_AUTO_PLAY, autoPlay)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            song = it.getParcelable(ARG_SONG)
            autoPlay = it.getBoolean(ARG_AUTO_PLAY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SongDetailBottomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isDraggable = true
            isHideable = true
            peekHeight = resources.displayMetrics.heightPixels
            skipCollapsed = true
        }
        binding.down.setOnClickListener { dismiss() }

        // Initialize song UI and functionality
        song?.let { songData ->
            binding.title.text = songData.title
            binding.artist.text = songData.artist.name
            Glide.with(requireContext())
                .load(songData.album.cover_medium)
                .into(binding.cover)

            setupPlayPauseButton()
            setupSeekBar()
        }

        // Auto-play if required and if a new song is passed
        if (autoPlay && musicService?.currentSong != song) {
            musicService?.playSong(song!!)
        }
    }

    private fun setupPlayPauseButton() {
        binding.btnPlayPause.setOnClickListener {
            if (musicService?.isPlaying == true) {
                musicService?.pausePlayback()
                sharedViewModel.setPlayingState(false)
            } else {
                musicService?.resumePlayback()
                sharedViewModel.setPlayingState(true)
            }
            updatePlayPauseButton()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && musicService?.mediaPlayer != null) {
                    musicService?.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Start the seek bar update task
        handler.post(updateSeekBarTask)
    }

    private fun updatePlayPauseButton() {
        val isPlaying = musicService?.isPlaying ?: false
        binding.btnPlayPause.setImageResource(
            if (isPlaying) R.drawable.pause else R.drawable.play
        )
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val musicBinder = binder as MusicService.MusicBinder
            musicService = musicBinder.getService()
            isBound = true

            // Ensure button state reflects current playback state
            updatePlayPauseButton()

            // Initialize SeekBar to match current position when the service connects
            if (musicService?.mediaPlayer?.isPlaying == true) {
                val currentPosition = musicService?.mediaPlayer?.currentPosition ?: 0
                val duration = musicService?.mediaPlayer?.duration ?: 0
                binding.seekBar.max = duration
                binding.seekBar.progress = currentPosition
            }

            // Start seek bar updates when service is connected
            handler.post(updateSeekBarTask)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            handler.removeCallbacks(updateSeekBarTask)  // Stop updates when service is disconnected
        }
    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(requireContext(), MusicService::class.java)
        requireContext().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            requireContext().unbindService(serviceConnection)
            isBound = false
        }
        handler.removeCallbacks(updateSeekBarTask)  // Stop seek bar updates
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun formatTime(timeMs: Int): String {
        val minutes = (timeMs / 1000) / 60
        val seconds = (timeMs / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}