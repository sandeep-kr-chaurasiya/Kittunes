package com.kittunes

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kittunes.Api.ApiInterface
import com.kittunes.Api_Data.Data
import com.kittunes.Api_Data.MyData
import com.kittunes.databinding.FragmentSearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: SearchAdapter
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingSong: Data? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SearchAdapter(emptyList(), ::playSong, ::pauseSong)
        binding.recyclerView.adapter = adapter

        binding.searchbar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchSongs(it) }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    clearSearchResults()
                } else {
                    searchSongs(newText)
                }
                return true
            }
        })
    }
    //-------------------------Hit the api when user search for a song---------------------//
    private fun searchSongs(query: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://deezerdevs-deezer.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)

            val call = retrofit.getdata(query)
            call.enqueue(object : Callback<MyData?> {
            override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {
                if (response.isSuccessful) {
                    val dataList = response.body()?.data ?: emptyList()
                    adapter.updateData(dataList)
                } else {
                    Log.e("SearchFragment", "Response unsuccessful: ${response.errorBody()}")
                    Toast.makeText(requireContext(), "Error loading search results", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<MyData?>, t: Throwable) {
                Log.e("SearchFragment", "API call failed: ${t.message}")
                Toast.makeText(requireContext(), "Failed to load search results", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun clearSearchResults() {
        adapter.updateData(emptyList())
    }

    private fun playSong(song: Data) {
        if (currentPlayingSong == song) {
            mediaPlayer?.start()

        }else{
            releaseMediaPlayer()
            mediaPlayer = MediaPlayer().apply {
            setDataSource(song.preview)
            setOnPreparedListener { start() }
            setOnCompletionListener {
                currentPlayingSong = null
                adapter.notifyDataSetChanged()
            }
            prepareAsync()
        }
        currentPlayingSong = song
    }
    }

    private fun pauseSong(song: Data) {
        if (currentPlayingSong == song && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }
    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentPlayingSong = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseMediaPlayer()
    }
}