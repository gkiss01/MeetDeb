package com.gkiss01.meetdeb.adapter

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

enum class ViewTypes {
    ITEM_VIEW_TYPE_EVENT,
    ITEM_VIEW_TYPE_DATE,
    ITEM_VIEW_TYPE_PARTICIPANT,
    ITEM_VIEW_TYPE_HEADER,
    ITEM_VIEW_TYPE_LOADER,
    ITEM_VIEW_TYPE_ADDITION
}

abstract class AdapterClass: ListAdapter<DataItem, RecyclerView.ViewHolder>(AdapterDiffCallback()) {
    val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.EventItem -> ViewTypes.ITEM_VIEW_TYPE_EVENT.ordinal
            is DataItem.DateItem -> ViewTypes.ITEM_VIEW_TYPE_DATE.ordinal
            is DataItem.ParticipantItem -> ViewTypes.ITEM_VIEW_TYPE_PARTICIPANT.ordinal
            is DataItem.Header -> ViewTypes.ITEM_VIEW_TYPE_HEADER.ordinal
            is DataItem.Loader -> ViewTypes.ITEM_VIEW_TYPE_LOADER.ordinal
            is DataItem.Addition -> ViewTypes.ITEM_VIEW_TYPE_ADDITION.ordinal
        }
    }
}