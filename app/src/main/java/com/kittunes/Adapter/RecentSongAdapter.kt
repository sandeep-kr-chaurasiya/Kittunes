package com.kittunes.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kittunes.Api_Data.Data
import com.kittunes.databinding.RecentCardBinding

class RecentSongAdapter(
    private val recentSongs: List<Data>,
    private val onSongClicked: (Data) -> Unit
) : RecyclerView.Adapter<RecentSongAdapter.ViewHolder>() {

    private var songList: List<Data> = emptyList()

    inner class ViewHolder(private val binding: RecentCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Bind data to views
        fun bind(song: Data) {
            binding.songTitle.text = song.title_short
            binding.artistName.text = song.artist.name
            Glide.with(binding.root.context)
                .load(song.album.cover_medium)
                .into(binding.songThumbnail)
            binding.root.setOnClickListener {
                onSongClicked(song)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = recentSongs[position]
        holder.bind(song)
    }

    override fun getItemCount(): Int {
        return recentSongs.size
    }
}