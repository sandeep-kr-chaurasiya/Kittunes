package com.kittunes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kittunes.databinding.SearchCardBinding

class SearchAdapter(
    private var dataList: List<Data>,
    private val onPlayClick: (Data) -> Unit,
    private val onPauseClick: (Data) -> Unit,
    ):RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: SearchCardBinding = SearchCardBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = SearchCardBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = dataList[position]
        holder.binding.songTitle.text = song.title
        holder.binding.artistName.text = song.artist.name

//-------------------------load the image of the song using Glide---------------------//
        Glide.with(holder.itemView.context).load(song.album.cover_medium).into(holder.binding.songThumbnail)

//-------------------------working from search fragment by extending the class---------------------//

        holder.binding.playButton.setOnClickListener {
            onPlayClick(song)
            holder.binding.playButton.visibility = View.GONE
            holder.binding.pauseButton.visibility = View.VISIBLE
        }

        holder.binding.pauseButton.setOnClickListener {
            onPauseClick(song)
            holder.binding.playButton.visibility = View.VISIBLE
            holder.binding.pauseButton.visibility = View.GONE
        }

    }

    fun updateData(newDataList: List<Data>) {
        dataList = newDataList
        notifyDataSetChanged()
    }
}