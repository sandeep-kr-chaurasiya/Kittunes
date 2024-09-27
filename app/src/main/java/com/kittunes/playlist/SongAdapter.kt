package com.kittunes.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kittunes.api_response.search.Data
import com.kittunes.databinding.SongCardBinding

class SongAdapter(
    private val songs: List<Data>,
    private val onSongClicked: (Data) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(private val binding: SongCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Data) {
            binding.songTitle.text = song.title
            binding.artistName.text=song.artist.name

            Glide.with(binding.root.context)
                .load(song.album.cover_medium)
                .into(binding.songThumbnail)

            binding.root.setOnClickListener {
                onSongClicked(song)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = SongCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun getItemCount(): Int = songs.size

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.bind(song)
    }
}