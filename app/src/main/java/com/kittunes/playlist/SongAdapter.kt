package com.kittunes.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kittunes.Api_Data.Data
import com.kittunes.databinding.SongCardBinding

class SongAdapter(
    private val songs: List<Data>,
    private val onSongClick: (Data) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(private val binding: SongCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Data) {
            binding.songTitle.text = song.title
            binding.root.setOnClickListener {
                onSongClick(song)
            }
            Glide.with(binding.root.context)
                .load(song.album.cover_medium)
                .into(binding.songThumbnail)
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