package com.kittunes.fragments

import android.os.Bundle
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

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

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
            // Handle empty or null list here (e.g., show a message or empty state)
            Log.d("HomeFragment", "No recent songs available")
        } else {
            updateUIWithSong(songList)
        }
    }

    private fun updateUIWithSong(songlist: List<Data>) {
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recentRecyclerview.layoutManager = layoutManager

        binding.recentRecyclerview.adapter = RecentSongAdapter(songlist) { song ->
            Log.d("HomeFragment", "Clicked song: ${song.title}")
            // Handle song click event here
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}