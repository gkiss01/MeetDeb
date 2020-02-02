package com.gkiss01.meetdeb.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.data.Participant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ParticipantEntryAdapter: AdapterClass() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewTypes.ITEM_VIEW_TYPE_PARTICIPANT.ordinal -> ParticipantViewHolder.from(parent)
            ViewTypes.ITEM_VIEW_TYPE_LOADER.ordinal -> LoaderViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ParticipantViewHolder -> {
                val participantItem = getItem(position) as DataItem.ParticipantItem
                holder.bind(participantItem.participant)
            }
        }
    }

    fun addLoading() {
        adapterScope.launch {
            val items = listOf(DataItem.Loader)

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    fun addParticipants(list: List<Participant>) {
        adapterScope.launch {
            val items =  list.map { DataItem.ParticipantItem(it) }

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }
}
