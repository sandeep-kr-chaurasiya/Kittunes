package com.kittunes.fragments

import com.kittunes.player.SharedViewModel
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
import com.kittunes.player.MusicService

class SongDetailBottomFragment : BottomSheetDialogFragment() {

    private var _binding: SongDetailBottomBinding? = null
    private val binding get() = _binding!!

    private var musicService: MusicService? = null
    private var isBound = false
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val handler = Handler()
    private val updateSeekBarTask = object : Runnable {
        override fun run() {
            musicService?.mediaPlayer?.let { mediaPlayer ->
                binding.seekBar.max = mediaPlayer.duration
                binding.seekBar.progress = mediaPlayer.currentPosition
                binding.currentTime.text = formatTime(mediaPlayer.currentPosition)
                binding.duration.text = formatTime(mediaPlayer.duration)
                handler.postDelayed(this, 1000)  // Update every second
            }
        }
    }

    companion object {
        private const val ARG_SONG = "arg_song"
        private const val ARG_AUTO_PLAY = "arg_auto_play"

        fun newInstance(song: Data, autoPlay: Boolean): SongDetailBottomFragment {
            return SongDetailBottomFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_SONG, song)
                    putBoolean(ARG_AUTO_PLAY, autoPlay)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModelData()
    }

    private fun observeViewModelData() {
        sharedViewModel.currentSong.observe(this) { song ->
            song?.let { updateSongUI(it) }
        }

        sharedViewModel.isPlaying.observe(this) { isPlaying ->
            updatePlayPauseButton(isPlaying)
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
        setupClickListeners()
        setupSeekBar()
    }

    private fun setupClickListeners() {
        binding.down.setOnClickListener { dismiss() }
        binding.btnNext.setOnClickListener {
            sharedViewModel.playNextSong()
            prepareAndPlaySong()
        }
        binding.btnPrevious.setOnClickListener {
            sharedViewModel.playPreviousSong()
            prepareAndPlaySong()
        }
        binding.btnPlayPause.setOnClickListener { togglePlayPause() }
    }

    private fun togglePlayPause() {
        musicService?.let {
            if (it.isPlaying) {
                it.pausePlayback()
                sharedViewModel.setPlayingState(false)
            } else {
                it.resumePlayback()
                sharedViewModel.setPlayingState(true)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && isBound) {
                    musicService?.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        binding.btnPlayPause.setImageResource(
            if (isPlaying) R.drawable.pause else R.drawable.play
        )
    }

    private fun updateSongUI(song: Data) {
        binding.title.text = song.title_short ?: "Unknown Title"
        binding.artist.text = song.artist.name ?: "Unknown Artist"
        Glide.with(requireContext())
            .load(song.album.cover_medium)
            .into(binding.cover)

        // Update the SeekBar based on the current song
        musicService?.mediaPlayer?.let { mediaPlayer ->
            binding.seekBar.max = mediaPlayer.duration
            binding.seekBar.progress = mediaPlayer.currentPosition
        }

        handler.post(updateSeekBarTask)
    }

    private fun prepareAndPlaySong() {
        if (isBound) {
            sharedViewModel.currentSong.value?.let { song ->
                if (musicService?.currentSong != song) {
                    musicService?.prepareSong(song)
                }
                musicService?.mediaPlayer?.setOnPreparedListener {
                    if (sharedViewModel.isPlaying.value == true) {
                        musicService?.startPlayback()
                    }
                }
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val musicBinder = binder as? MusicService.MusicBinder
            musicService = musicBinder?.getService()
            isBound = true
            sharedViewModel.currentSong.value?.let { song ->
                updateSongUI(song)
            }
            updatePlayPauseButton(sharedViewModel.isPlaying.value ?: false)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            handler.removeCallbacks(updateSeekBarTask)
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
        handler.removeCallbacks(updateSeekBarTask)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(timeMs: Int): String {
        val minutes = (timeMs / 1000) / 60
        val seconds = (timeMs / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}