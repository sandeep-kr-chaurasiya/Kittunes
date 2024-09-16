package com.kittunes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
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

        setupMenu() // Set up the menu for playlist options
    }

    private fun displayPlaylistDetails(playlist: Playlist) {
        Log.d("PlaylistDetailFragment", "Displaying playlist: ${playlist.playlistName}")
        binding.playlistName.text = playlist.playlistName
        fetchSongsForPlaylist(playlist)
    }

    private fun fetchSongsForPlaylist(playlist: Playlist) {
        // Placeholder for implementation to fetch and display songs for the given playlist
        Log.d("PlaylistDetailFragment", "Fetching songs for playlist: ${playlist.playlistName}")
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
                    Log.e("PlaylistDetailFragment", "Error fetching playlists: ${e.message}")
                    Toast.makeText(requireContext(), "Error fetching playlists", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("PlaylistDetailFragment", "Error: User not logged in")
            Toast.makeText(requireContext(), "Error: User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUIWithPlaylists(playlists: List<Playlist>) {
        val adapter = PlaylistAdapter(playlists) { playlist ->
            Log.d("PlaylistDetailFragment", "Clicked playlist: ${playlist.playlistName}")
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

    private fun setupMenu() {
        binding.playlistMenuButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(requireContext(), view)
            popupMenu.menuInflater.inflate(R.menu.playlist_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.delete_playlist -> {
                        playlist?.let { deletePlaylist(it) }
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    private fun deletePlaylist(playlist: Playlist) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val playlistId = playlist.playlistId // Get the unique ID from the playlist object
            Log.d("PlaylistDetailFragment", "Attempting to delete playlist with ID: $playlistId")

            if (playlistId != null) {
                val playlistRef = db.collection("Playlists").document(userId).collection("UserPlaylists").document(playlistId)

                playlistRef.delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Playlist deleted successfully", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack() // Go back to the previous fragment
                    }
                    .addOnFailureListener { e ->
                        Log.e("PlaylistDetailFragment", "Error deleting playlist: ${e.message}")
                        Toast.makeText(requireContext(), "Error deleting playlist", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Log.e("PlaylistDetailFragment", "Error: Playlist ID is null")
                Toast.makeText(requireContext(), "Error: Playlist ID is null", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("PlaylistDetailFragment", "Error: User not logged in")
            Toast.makeText(requireContext(), "Error: User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}