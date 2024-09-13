package com.kittunes.fragments

import SharedViewModel
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
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
import com.kittunes.Api_Data.Data
import com.kittunes.R
import com.kittunes.databinding.SongDetailBottomBinding

class SongDetailBottomFragment : BottomSheetDialogFragment() {

    private var _binding: SongDetailBottomBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var mediaPlayer: MediaPlayer
    private val handler = Handler(Looper.getMainLooper())
    private var updateSeekBar: Runnable? = null

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

        val song = arguments?.getParcelable<Data>("song") ?: return

        // Configure bottom sheet behavior
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isDraggable = true
            isHideable = true
            peekHeight = resources.displayMetrics.heightPixels
            skipCollapsed = true
        }

        // Set up UI
        binding.title.text = song.title
        binding.artist.text = song.artist.name
        Glide.with(this).load(song.album.cover_medium).into(binding.cover)

        // Set up button listeners
        binding.down.setOnClickListener { dismiss() }
        binding.btnPlayPause.setOnClickListener { togglePlayback() }

        // Initialize seek bar
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Initialize and start seek bar updates
        updateSeekBar = object : Runnable {
            override fun run() {
                if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                    binding.seekBar.progress = mediaPlayer.currentPosition
                    binding.currenttime.text = formatTime(mediaPlayer.currentPosition)
                    handler.postDelayed(this, 1000)
                }
            }
        }

        // Observe current song changes
        sharedViewModel.currentSong.observe(viewLifecycleOwner) { currentSong ->
            currentSong?.let { playSong(it) }
        }
    }

    private fun playSong(song: Data) {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer().apply {
            setDataSource(song.preview)
            setOnPreparedListener {
                seekTo(sharedViewModel.currentPosition.value ?: 0)
                start()
                binding.seekBar.max = duration
                handler.post(updateSeekBar!!)
            }
            setOnCompletionListener {
                handler.removeCallbacks(updateSeekBar!!)
                sharedViewModel.setCurrentSong(null)
                sharedViewModel.setCurrentPosition(0)
            }
            prepareAsync()
        }
    }

    private fun togglePlayback() {
        if (::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                sharedViewModel.setCurrentPosition(mediaPlayer.currentPosition)
                binding.btnPlayPause.setImageResource(R.drawable.play)
            } else {
                mediaPlayer.start()
                sharedViewModel.setCurrentPosition(mediaPlayer.currentPosition)
                binding.btnPlayPause.setImageResource(R.drawable.pause)
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

        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        playSong(song)
    }

    companion object {
        @JvmStatic
        fun newInstance(song: Data) = SongDetailBottomFragment().apply {
            arguments = Bundle().apply {
                putParcelable("song", song)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        handler.removeCallbacks(updateSeekBar ?: return)
    }
}