// SongDetailBottomFragment.kt
package com.kittunes.fragments

import SharedViewModel
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firestore.v1.FirestoreGrpc.bindService
import com.kittunes.Api_Data.Data
import com.kittunes.R
import com.kittunes.databinding.SongDetailBottomBinding
import com.kittunes.services.MusicService

class SongDetailBottomFragment : BottomSheetDialogFragment() {

    private var _binding: SongDetailBottomBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var musicService: MusicService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val musicBinder = binder as MusicService.MusicBinder
            musicService = musicBinder.getService()
            isBound = true
            // Update UI or state as needed
            arguments?.getParcelable<Data>("song")?.let { song ->
                updateSongData(song)
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

        // Configure bottom sheet behavior
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isDraggable = true
            isHideable = true
            peekHeight = resources.displayMetrics.heightPixels
            skipCollapsed = true
        }

        // Initialize UI
        arguments?.getParcelable<Data>("song")?.let { song ->
            updateSongData(song)
        }

        // Set up button listeners
        binding.down.setOnClickListener { dismiss() }
        binding.btnPlayPause.setOnClickListener { togglePlayback() }

        // Initialize seek bar
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && isBound) musicService?.mediaPlayer?.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Observe current song and position changes
        sharedViewModel.currentSong.observe(viewLifecycleOwner) { currentSong ->
            currentSong?.let { updateSongData(it) }
        }

        sharedViewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            if (isBound) musicService?.mediaPlayer?.seekTo(position ?: 0)
        }
    }

    private fun togglePlayback() {
        if (isBound) {
            musicService?.let { service ->
                if (service.isPlaying) {
                    service.pausePlayback()
                    sharedViewModel.setCurrentPosition(service.mediaPlayer?.currentPosition ?: 0)
                    binding.btnPlayPause.setImageResource(R.drawable.play)
                } else {
                    service.resumePlayback()
                    sharedViewModel.setCurrentPosition(service.mediaPlayer?.currentPosition ?: 0)
                    binding.btnPlayPause.setImageResource(R.drawable.pause)
                }
            }
        }
    }

    private fun formatTime(ms: Int): String {
        val minutes = (ms / 1000) / 60
        val seconds = (ms / 1000) % 60
        return "${minutes}:${seconds.toString().padStart(2, '0')}"
    }

    fun updateSongData(song: Data) {
        binding.title.text = song.title
        binding.artist.text = song.artist.name
        Glide.with(this).load(song.album.cover_medium).into(binding.cover)

        if (isBound) {
            musicService?.playSong(song)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(song: Data) = SongDetailBottomFragment().apply {
            arguments = Bundle().apply {
                putParcelable("song", song)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to the MusicService
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
        _binding = null
        // Remove callbacks if needed
    }
}