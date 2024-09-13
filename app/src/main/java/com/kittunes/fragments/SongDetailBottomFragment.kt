package com.kittunes.fragments

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kittunes.Api_Data.Data
import com.kittunes.databinding.SongDetailBottomBinding
import com.kittunes.R

class SongDetailBottomFragment : BottomSheetDialogFragment() {

    private var _binding: SongDetailBottomBinding? = null
    private val binding get() = _binding!!

    private var mediaPlayer: MediaPlayer? = null
    private var currentSong: Data? = null
    private var currentPlayingSong: Data? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SongDetailBottomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure BottomSheetBehavior
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isDraggable = true
            isHideable = true
            peekHeight = resources.displayMetrics.heightPixels
            skipCollapsed = true
        }

        // Initialize UI with the song data if available
        arguments?.getParcelable<Data>(ARG_SONG)?.let { song ->
            currentSong = song
            updateUI(song)
        }

        binding.down.setOnClickListener { dismiss() }

        binding.btnPlayPause.setOnClickListener {
            currentSong?.let { song ->
                if (currentPlayingSong == song) {
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.pause()
                        binding.btnPlayPause.setImageResource(R.drawable.play) // Update icon
                    } else {
                        mediaPlayer?.start()
                        binding.btnPlayPause.setImageResource(R.drawable.pause) // Update icon
                    }
                } else {
                    releaseMediaPlayer()
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(song.preview)
                        setOnPreparedListener { start() }
                        setOnCompletionListener {
                            currentPlayingSong = null
                            binding.btnPlayPause.setImageResource(R.drawable.play) // Update icon
                        }
                        prepareAsync()
                    }
                    currentPlayingSong = song
                    binding.btnPlayPause.setImageResource(R.drawable.pause) // Update icon
                }
            }
        }
        val durationInSeconds = currentSong?.duration ?: 0
        val minutes = durationInSeconds / 60
        val seconds = durationInSeconds % 60
        val formattedDuration = String.format("%02d:%02d", minutes, seconds)

        binding.duration.text = formattedDuration
    }

    fun updateSongData(song: Data) {
        currentSong = song
        updateUI(song)
    }

    private fun updateUI(song: Data) {
        binding.title.text = song.title
        binding.artist.text = song.artist.name
        Glide.with(this).load(song.album.cover_medium).into(binding.cover)
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseMediaPlayer()
        _binding = null
    }

    companion object {
        private const val ARG_SONG = "arg_song"

        fun newInstance(song: Data): SongDetailBottomFragment {
            return SongDetailBottomFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_SONG, song)
                }
            }
        }
    }
}