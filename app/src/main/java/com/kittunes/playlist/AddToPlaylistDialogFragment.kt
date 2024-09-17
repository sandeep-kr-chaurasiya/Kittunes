package com.kittunes.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.kittunes.Api_Data.Playlist
import com.kittunes.databinding.DailougeAddToPlaylistBinding
import com.kittunes.fragments.PlaylistAdapter

class AddToPlaylistDialogFragment(
    private val playlists: List<Playlist>,
    private val onPlaylistSelected: (Playlist) -> Unit
) : DialogFragment() {

    private lateinit var binding: DailougeAddToPlaylistBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DailougeAddToPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        binding.dailougeRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.dailougeRecyclerview.adapter = PlaylistAdapter(playlists) { selectedPlaylist ->
            onPlaylistSelected(selectedPlaylist)
            dismiss() // Close the dialog after selection
        }
    }
}