package com.gkiss01.meetdeb.network.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.databinding.AlbumsListItemBinding
import com.gkiss01.meetdeb.network.data.AlbumProperty

class AlbumAdapter: ListAdapter<AlbumProperty, AlbumAdapter.AlbumViewHolder>(AlbumDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        return AlbumViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class AlbumViewHolder(private val binding: AlbumsListItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AlbumProperty) {
            binding.album = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): AlbumViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AlbumsListItemBinding.inflate(layoutInflater, parent, false)
                return AlbumViewHolder(binding)
            }
        }
    }
}