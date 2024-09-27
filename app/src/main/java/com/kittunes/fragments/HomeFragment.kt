package com.kittunes.fragments

import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kittunes.adapter.RecentSongAdapter
import com.kittunes.adapter.RecommendationAdapter
import com.kittunes.api.SearchApiInterface
import com.kittunes.api_response.search.Data
import com.kittunes.api_response.search.MyData
import com.kittunes.databinding.FragmentHomeBinding
import com.kittunes.main.MainActivity
import com.kittunes.player.MusicService
import com.kittunes.player.SharedViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RecommendationAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var musicService: MusicService? = null
    private var isBound = false

    private val apiInterface by lazy {
        Retrofit.Builder()
            .baseUrl("https://deezerdevs-deezer.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SearchApiInterface::class.java)
    }

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
        setupRecyclerView()
        searchSongs("Emiway bantai")
        val songList = sharedViewModel.songList.value
        if (songList.isNullOrEmpty()) {
            Log.d("HomeFragment", "No recent songs available")
        } else {
            updateUIWithSong(songList)
        }
    }

    private fun setupRecyclerView() {
        binding.recommendationRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        adapter = RecommendationAdapter(
            onSongClicked = { song -> onSongClicked(song) },
            onAddToQueue = { song -> sharedViewModel.addSongToQueue(song) },
            onClickAddToPlaylist = {   /* Add implementation if needed */ }
        )
        binding.recommendationRecyclerview.adapter = adapter
    }

    private fun searchSongs(query: String) {
        apiInterface.getData(query).enqueue(object : Callback<MyData?> {
            override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {
                if (response.isSuccessful) {
                    val dataList = response.body()?.data ?: emptyList()
                    adapter.submitList(dataList)
                } else {
                    showError("Error loading search results: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<MyData?>, t: Throwable) {
                Log.e(TAG, "API call failed: ${t.message}", t)
                Toast.makeText(requireContext(), "Failed to load search results", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUIWithSong(songList: List<Data>) {
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val reversedSongList = songList.reversed()
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
        playSong(song)
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

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
