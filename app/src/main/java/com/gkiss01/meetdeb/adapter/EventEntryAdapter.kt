package com.gkiss01.meetdeb.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.EventEntry
import com.gkiss01.meetdeb.databinding.EventsListItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class EventEntryAdapter(val clickListener: EventClickListener): ListAdapter<DataItem, RecyclerView.ViewHolder>(EventEntryDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.EventItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> EntryViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EntryViewHolder -> {
                val eventItem = getItem(position) as DataItem.EventItem
                holder.bind(eventItem.eventEntry, clickListener)
            }
        }
    }

    fun addHeaderAndSubmitList(list: List<EventEntry>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.EventItem(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    class HeaderViewHolder(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.events_list_header, parent, false) as View
                return HeaderViewHolder(itemView)
            }
        }
    }

    class EntryViewHolder(private val binding: EventsListItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EventEntry, clickListener: EventClickListener) {
            binding.event = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): EntryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = EventsListItemBinding.inflate(layoutInflater, parent, false)
                return EntryViewHolder(binding)
            }
        }
    }
}

sealed class DataItem {
    abstract val id: Long

    data class EventItem(val eventEntry: EventEntry): DataItem() {
        override val id = eventEntry.entryId.toLong()
    }

    object Header: DataItem() {
        override val id = Long.MIN_VALUE
    }
}

class EventClickListener(val clickListener: (entryId: Int) -> Unit) {
    fun onClick(event: EventEntry) = clickListener(event.entryId)
}
