package com.kittunes.fragments

import android.app.AlertDialog
import android.content.ComponentName
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
import androidx.recyclerview.widget.RecyclerView
import com.kittunes.R
import com.kittunes.adapter.RecentSongAdapter
import com.kittunes.adapter.RecommendationAdapter
import com.kittunes.api.SearchApiInterface
import com.kittunes.api_response.search.Data
import com.kittunes.api_response.search.MyData
import com.kittunes.databinding.FragmentHomeBinding
import com.kittunes.main.MainActivity
import com.kittunes.player.MusicService
import com.kittunes.player.SharedViewModel
import com.kittunes.playlist.PlaylistAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recommendationAdapter: RecommendationAdapter
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
        setupRecyclerViews()
        searchSongs("Emiway Bantai")
        observeRecentSongs()
    }

    private fun setupRecyclerViews() {
        binding.recommendationRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            recommendationAdapter = RecommendationAdapter(
                onSongClicked = ::onSongClicked,
                onAddToQueue = sharedViewModel::addSongToQueue,
                onClickAddToPlaylist = ::onClickAddToPlaylist
            )
            adapter = recommendationAdapter
        }
    }

    private fun observeRecentSongs() {
        sharedViewModel.songList.observe(viewLifecycleOwner) { songList ->
            if (songList.isNullOrEmpty()) {
                Log.d("HomeFragment", "No recent songs available")
            } else {
                updateRecentSongsUI(songList)
            }
        }
    }

    private fun searchSongs(query: String) {
        apiInterface.getData(query).enqueue(object : Callback<MyData?> {
            override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {
                if (response.isSuccessful) {
                    val dataList = response.body()?.data.orEmpty()
                    recommendationAdapter.submitList(dataList)
                } else {
                    showError("Error loading search results: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<MyData?>, t: Throwable) {
                showError("Failed to load search results")
                Log.e("HomeFragment", "API call failed: ${t.message}", t)
            }
        })
    }

    private fun updateRecentSongsUI(songList: List<Data>) {
        val recentSongsAdapter = RecentSongAdapter(songList.reversed()) { song ->
            onSongClicked(song)
            sharedViewModel.addSongToQueue(song)
        }
        binding.recentRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = recentSongsAdapter
        }
    }

    private fun onSongClicked(song: Data) {
        sharedViewModel.onSongClicked(song, requireActivity() as MainActivity, isBound)
        if (isBound) {
            playSong(song)
        } else {
            bindMusicServiceAndPlaySong(song)
        }
    }

    private fun playSong(song: Data) {
        musicService?.apply {
            prepareSong(song)
            mediaPlayer?.setOnPreparedListener {
                startPlayback()
            }
        }
    }

    private fun bindMusicServiceAndPlaySong(song: Data) {
        val serviceIntent = Intent(requireContext(), MusicService::class.java)
        requireContext().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        playSong(song)
    }

    private fun onClickAddToPlaylist(song: Data) {
        Log.d("HomeFragment", "Add to playlist clicked for song: ${song.title}")
        showAddToPlaylistDialog(song)
    }

    private fun showAddToPlaylistDialog(song: Data) {
        sharedViewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            if (playlists.isEmpty()) {
                Toast.makeText(requireContext(), "No playlists available", Toast.LENGTH_SHORT).show()
                return@observe
            }

            val dialogView = layoutInflater.inflate(R.layout.dailouge_add_to_playlist, null)
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Select Playlist")
                .setView(dialogView)
                .create()

            dialogView.findViewById<RecyclerView>(R.id.dailougeRecyclerview).apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = PlaylistAdapter(playlists) { selectedPlaylist ->
                    selectedPlaylist.playlistId?.let {
                        sharedViewModel.addSongToPlaylist(it, song)
                        Toast.makeText(requireContext(), "Added to playlist", Toast.LENGTH_SHORT).show()
                    } ?: showError("Error: Playlist ID is missing")
                    dialog.dismiss()
                }
            }
            dialog.show()
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