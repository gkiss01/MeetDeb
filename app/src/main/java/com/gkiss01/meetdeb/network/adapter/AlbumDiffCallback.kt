package com.gkiss01.meetdeb.network.adapter

import androidx.recyclerview.widget.DiffUtil
import com.gkiss01.meetdeb.network.data.AlbumProperty

class AlbumDiffCallback: DiffUtil.ItemCallback<AlbumProperty>() {
    override fun areItemsTheSame(oldItem: AlbumProperty, newItem: AlbumProperty): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: AlbumProperty, newItem: AlbumProperty): Boolean {
        return oldItem == newItem
    }
}