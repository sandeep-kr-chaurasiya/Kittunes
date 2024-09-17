package com.kittunes.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kittunes.Api_Data.Data
import com.kittunes.R
import com.kittunes.databinding.SongCardBinding

class SongAdapter(
    private val songs: List<Data>,
    private val onSongClicked: (Data) -> Unit // Changed to Data to match your data type
) : RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SongCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]
        holder.bind(song)
    }

    override fun getItemCount(): Int = songs.size

    inner class ViewHolder(private val binding: SongCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Data) {
            binding.songTitle.text = song.title
            binding.artistName.text = song.artist.name // Adjust based on actual Artist class

            // Load the song thumbnail using Glide
            Glide.with(binding.root.context)
                .load(song.preview) // Assuming `preview` is the image URL
                .placeholder(R.drawable.dummyimage) // Optional placeholder
                .into(binding.songThumbnail)

            binding.root.setOnClickListener {
                onSongClicked(song)
            }
        }
    }
}