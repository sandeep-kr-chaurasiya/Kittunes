package com.kittunes.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kittunes.Api_Data.Data
import com.kittunes.databinding.FragmentHomeBinding
import com.kittunes.player.SharedViewModel
import com.kittunes.Adapter.RecentSongAdapter
import com.kittunes.main.MainActivity
import com.kittunes.player.MusicService

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var musicService: MusicService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val songList = sharedViewModel.songList.value
        if (songList.isNullOrEmpty()) {
            Log.d("HomeFragment", "No recent songs available")
        } else {
            updateUIWithSong(songList)
        }
    }

    private fun updateUIWithSong(songList: List<Data>) {
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val reversedSongList = songList.reversed() // Reverse the song list
        val adapter = RecentSongAdapter(reversedSongList) { song ->
            onSongClicked(song)
            sharedViewModel.addSongToQueue(song)
            playSong(song)
        }

        binding.recentRecyclerview.layoutManager = layoutManager
        binding.recentRecyclerview.adapter = adapter
    }
    private fun onSongClicked(song: Data) {
        sharedViewModel.onSongClicked(song, requireActivity() as MainActivity, isBound)
    }
    private fun playSong(song: Data) {
        if (isBound) {
            musicService?.prepareSong(song)
            musicService?.mediaPlayer?.setOnPreparedListener {
                musicService?.startPlayback()
            }
        } else {
            val serviceIntent = Intent(requireContext(), MusicService::class.java)
            requireContext().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isBound) {
            requireContext().unbindService(serviceConnection)
            isBound = false
        }
        _binding = null
    }
}