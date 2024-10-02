package com.kittunes.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kittunes.R
import com.kittunes.api_response.search.Data
import com.kittunes.databinding.SongCardBinding

class SongAdapter(
    private val songs: List<Data>,
    private val onSongClicked: (Data) -> Unit,
    private val onAddToQueue: (Data) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(private val binding: SongCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Data) {
            binding.songTitle.text = song.title
            binding.artistName.text = song.artist.name

            Glide.with(binding.root.context)
                .load(song.album.cover_medium)
                .into(binding.songThumbnail)

            binding.root.setOnClickListener {
                onSongClicked(song)
            }

            binding.menuButton.setOnClickListener { view ->
                val popupMenu = PopupMenu(binding.root.context, view)
                popupMenu.menuInflater.inflate(R.menu.playlist_song_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.add_queue -> {
                            onAddToQueue(song)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
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