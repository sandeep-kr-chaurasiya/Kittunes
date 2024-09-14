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
import android.os.Looper

class SongDetailBottomFragment : BottomSheetDialogFragment() {

    private var _binding: SongDetailBottomBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var musicService: MusicService? = null
    private var isBound = false
    private var shouldAutoPlay: Boolean = false

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeTask = object : Runnable {
        override fun run() {
            if (isBound && musicService?.mediaPlayer != null) {
                val currentPosition = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBar.progress = currentPosition
                binding.currentTime.text = formatTime(currentPosition)
                handler.postDelayed(this, 1000)
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val musicBinder = binder as MusicService.MusicBinder
            musicService = musicBinder.getService()
            isBound = true
            arguments?.getParcelable<Data>("song")?.let { song ->
                updateSongData(song)
            }
            // Start or resume updating the seek bar based on the playback state
            if (isBound && musicService?.isPlaying == true) {
                handler.post(updateTimeTask)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SongDetailBottomBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isDraggable = true
            isHideable = true
            peekHeight = resources.displayMetrics.heightPixels
            skipCollapsed = true
        }

        // Set up button listeners
        binding.down.setOnClickListener { dismiss() }
        binding.btnPlayPause.setOnClickListener { togglePlayback() }

        // Initialize seek bar
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && isBound) {
                    musicService?.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateTimeTask)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (isBound) {
                    musicService?.mediaPlayer?.seekTo(seekBar?.progress ?: 0)
                    handler.post(updateTimeTask)
                }
            }
        })

        // Observe SharedViewModel for updates
        sharedViewModel.currentSong.observe(viewLifecycleOwner) { currentSong ->
            currentSong?.let { updateSongData(it) }
        }

        sharedViewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            if (isBound && position != null) {
                binding.seekBar.progress = position
                binding.currentTime.text = formatTime(position)
            }
        }

        // Update button based on playback state
        updatePlayPauseButton()
    }

    private fun togglePlayback() {
        if (isBound) {
            musicService?.let { service ->
                if (service.isPlaying) {
                    service.pausePlayback()
                    binding.btnPlayPause.setImageResource(R.drawable.play)
                    handler.removeCallbacks(updateTimeTask)
                } else {
                    service.resumePlayback()
//                    service.mediaPlayer?.currentPosition?.let {sharedViewModel.setCurrentPosition(it)}
                    binding.btnPlayPause.setImageResource(R.drawable.pause)
                    handler.post(updateTimeTask)
                }
            }
        }
    }

    private fun updatePlayPauseButton() {
        val isPlaying = musicService?.isPlaying ?: false
        binding.btnPlayPause.setImageResource(
            if (isPlaying) R.drawable.pause else R.drawable.play
        )
    }

    private fun formatTime(ms: Int): String {
        val minutes = ms / 60000
        val seconds = (ms % 60000) / 1000
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateSongData(song: Data) {
        binding.title.text = song.title
        binding.artist.text = song.artist.name
        Glide.with(this).load(song.album.cover_medium).into(binding.cover)

        binding.duration.text = formatTime(song.duration)
        binding.seekBar.max = song.duration

        // Do not automatically start playback
        if (isBound && shouldAutoPlay) {
            musicService?.playSong(song)
            updatePlayPauseButton()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(song: Data, autoPlay: Boolean = false) = SongDetailBottomFragment().apply {
            arguments = Bundle().apply {
                putParcelable("song", song)
                putBoolean("autoPlay", autoPlay)
            }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateTimeTask)
        _binding = null
    }
}