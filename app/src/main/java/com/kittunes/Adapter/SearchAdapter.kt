package com.kittunes.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kittunes.Api_Data.Data
import com.kittunes.databinding.SearchCardBinding

class SearchAdapter(private val onSongClicked: (Data) -> Unit) : ListAdapter<Data, SearchAdapter.SongViewHolder>(SongDiffCallback()) {

    inner class SongViewHolder(private val binding: SearchCardBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Data) {
            binding.songTitle.text = song.title
            binding.artistName.text = song.artist.name
            Glide.with(binding.root.context)
                .load(song.album.cover_medium)
                .into(binding.songThumbnail)

            binding.searchcard.setOnClickListener { onSongClicked(song) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = SearchCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
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