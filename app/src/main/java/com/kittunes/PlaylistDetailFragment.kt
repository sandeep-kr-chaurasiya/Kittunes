package com.kittunes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kittunes.Api_Data.Playlist
import com.kittunes.databinding.FragmentPlaylistDetailBinding
import com.kittunes.fragments.PlaylistAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlaylistDetailFragment : Fragment() {

    private lateinit var binding: FragmentPlaylistDetailBinding
    private var playlist: Playlist? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if a playlist was passed as an argument
        arguments?.getParcelable<Playlist>("playlist")?.let {
            playlist = it
            displayPlaylistDetails(it)
        } ?: fetchPlaylists()
    }

    private fun displayPlaylistDetails(playlist: Playlist) {
        Log.d("PlaylistFragment", "Displaying playlist: ${playlist.playlistName}")
        binding.playlistName.text = playlist.playlistName
        fetchSongsForPlaylist(playlist)
    }

    private fun fetchSongsForPlaylist(playlist: Playlist) {
        // Implementation to fetch and display songs for the given playlist
        // For now, just logging that this function is called
        println("Fetching songs for playlist: ${playlist.playlistName}")
        // Add your Firebase fetching logic here
    }

    private fun fetchPlaylists() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val userPlaylistsRef = db.collection("Playlists").document(userId).collection("UserPlaylists")

            userPlaylistsRef.get()
                .addOnSuccessListener { result ->
                    val playlists = mutableListOf<Playlist>()
                    for (document in result) {
                        val playlist = document.toObject(Playlist::class.java)
                        playlists.add(playlist)
                    }
                    updateUIWithPlaylists(playlists)
                }
                .addOnFailureListener { e ->
                    // Handle error
                    e.printStackTrace()
                }
        }
    }

    private fun updateUIWithPlaylists(playlists: List<Playlist>) {
        val adapter = PlaylistAdapter(playlists) { playlist ->
            Log.d("PlaylistFragment", "Clicked playlist: ${playlist.playlistName}")
            val detailFragment = PlaylistDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("playlist", playlist) // Pass the playlist data
                }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }
        binding.songsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.songsRecyclerView.adapter = adapter
    }
}