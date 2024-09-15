package com.kittunes.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kittunes.Api_Data.Data
import com.kittunes.R
import com.kittunes.databinding.SearchCardBinding

class SearchAdapter(private val onSongClicked: (Data) -> Unit) : ListAdapter<Data, SearchAdapter.SearchResultViewHolder>(SongDiffCallback()) {

    inner class SearchResultViewHolder(private val binding: SearchCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Data) {
            binding.songTitle.text = song.title ?: "Unknown Title"
            binding.artistName.text = song.artist.name ?: "Unknown Artist"
            Glide.with(binding.root.context)
                .load(song.album.cover_big)
                .placeholder(R.drawable.dummyimage)
                .error(R.drawable.dummyimage)
                .into(binding.songThumbnail)

            // Set the click listener for the song
            binding.searchcard.setOnClickListener {
                onSongClicked(song)  // Notify the callback with the clicked song
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = SearchCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

private class SongDiffCallback : DiffUtil.ItemCallback<Data>() {
    override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
        return oldItem == newItem
    }
}