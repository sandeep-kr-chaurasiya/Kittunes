package com.kittunes.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.kittunes.Api_Data.Playlist
import com.kittunes.playlist.PlaylistDetailFragment
import com.kittunes.R
import com.kittunes.databinding.FragmentLibraryBinding
import java.util.UUID

class LibraryFragment : Fragment() {

    private lateinit var binding: FragmentLibraryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.createPlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }
        fetchPlaylists()
    }

    private fun showCreatePlaylistDialog() {
        val dialogView = layoutInflater.inflate(R.layout.create_dialouge, null)
        val playlistNameEditText = dialogView.findViewById<EditText>(R.id.PlaylistName)
        val createPlaylistButton = dialogView.findViewById<MaterialButton>(R.id.buttonCreatePlaylist)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Create Playlist")
            .setView(dialogView)
            .create()

        createPlaylistButton.setOnClickListener {
            val playlistName = playlistNameEditText.text.toString().trim()
            if (playlistName.isNotEmpty()) {
                savePlaylistToFirebase(playlistName)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter a playlist name", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun savePlaylistToFirebase(playlistName: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val playlistId = UUID.randomUUID().toString() // Generate a unique ID
            val userPlaylistsRef = db.collection("Playlists").document(userId).collection("UserPlaylists")
            val playlist = hashMapOf(
                "playlistId" to playlistId,
                "playlistName" to playlistName,
                "createdAt" to FieldValue.serverTimestamp()
            )

            userPlaylistsRef.document(playlistId).set(playlist)
                .addOnSuccessListener {
                    Log.d(TAG, "Playlist created successfully")
                    fetchPlaylists() // Refresh the playlist after adding
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error: ${e.message}", e)
                }
        } else {
            Log.e(TAG, "Error: User not logged in")
        }
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
                    Toast.makeText(requireContext(), "Error fetching playlists: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Error: User not logged in", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateUIWithPlaylists(playlists: List<Playlist>) {
        binding.playlistRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistRecyclerView.adapter = PlaylistAdapter(playlists) { playlist ->
            Log.d("LibraryFragment", "Clicked playlist: ${playlist.playlistName}")
            val fragment = PlaylistDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("playlist", playlist) // Pass the playlist data
                }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
    companion object {
        private const val TAG = "LibraryFragment"
    }
}