package com.kittunes.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kittunes.api_response.search.Data
import com.kittunes.R
import com.kittunes.databinding.RowSearchCardBinding

class SearchAdapter(
    private val onSongClicked: (Data) -> Unit,
    private val onAddToQueue: (Data) -> Unit,
    private val onClickAddToPlaylist: (Data) -> Unit
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private var songList: List<Data> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<Data>) {
        songList = list
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowSearchCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songList[position]
        holder.bind(song)
    }

    override fun getItemCount(): Int = songList.size

    inner class ViewHolder(private val binding: RowSearchCardBinding) : RecyclerView.ViewHolder(binding.root) {
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
                popupMenu.menuInflater.inflate(R.menu.search_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.add_queue -> {
                            onAddToQueue(song)
                            true
                        }

                        R.id.search_add_to_playlist -> {
                            onClickAddToPlaylist(song)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }
    }
}