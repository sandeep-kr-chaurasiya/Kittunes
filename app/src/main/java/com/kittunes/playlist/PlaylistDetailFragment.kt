package com.kittunes.playlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.kittunes.Api_Data.Data
import com.kittunes.Api_Data.Playlist
import com.kittunes.databinding.FragmentPlaylistDetailBinding
import com.kittunes.player.SharedViewModel
import com.kittunes.R

class PlaylistDetailFragment : Fragment() {

    private lateinit var binding: FragmentPlaylistDetailBinding
    private var playlist: Playlist? = null
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        arguments?.getParcelable<Playlist>("playlist")?.let {
            playlist = it
            displayPlaylistDetails(it)
        } ?: fetchPlaylists()

        setupMenu()

        // Observe playlistSongs LiveData
        sharedViewModel.playlistSongs.observe(viewLifecycleOwner) { songs ->
            updateUIWithSongs(songs)
        }
    }

    private fun displayPlaylistDetails(playlist: Playlist) {
        Log.d("PlaylistDetailFragment", "Displaying playlist: ${playlist.playlistName}")
        binding.playlistName.text = playlist.playlistName
        fetchSongsForPlaylist(playlist)
    }

    private fun fetchSongsForPlaylist(playlist: Playlist) {
        playlist.playlistId?.let {
            sharedViewModel.fetchSongsFromPlaylist(it)
        } ?: Log.e("PlaylistDetailFragment", "Playlist ID is null")
    }

    private fun updateUIWithSongs(songs: List<Data>) {
        // Log the songs list to check its content
        Log.d("PlaylistDetailFragment", "Updating UI with songs: $songs")
        val adapter = SongAdapter(songs) { song ->
            Log.d("PlaylistDetailFragment", "Clicked song: ${song.title}")
            // Handle song click event
        }
        binding.songsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.songsRecyclerView.adapter = adapter
    }

    private fun fetchPlaylists() {
        sharedViewModel.fetchPlaylists()
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
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("PlaylistDetailFragment", "Error: User not logged in")
            return
        }

        sharedViewModel.firestore.collection("Playlists").document(userId).collection("UserPlaylists")
            .document(playlist.playlistId!!)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Playlist deleted", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Log.e("PlaylistDetailFragment", "Error deleting playlist", e)
            }
    }
}