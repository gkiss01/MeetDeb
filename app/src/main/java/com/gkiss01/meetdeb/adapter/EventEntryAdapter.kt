package com.gkiss01.meetdeb.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.network.GlideRequests
import com.gkiss01.meetdeb.network.NavigationCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1
private const val ITEM_VIEW_TYPE_LOADER = 2

class EventEntryAdapter(val glide: GlideRequests,
                        private val detailsClickListener: AdapterClickListener,
                        private val joinClickListener: AdapterClickListener,
                        private val anotherDateClickListener: AdapterClickListener): ListAdapter<DataItem, RecyclerView.ViewHolder>(AdapterDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.EventItem -> ITEM_VIEW_TYPE_ITEM
            is DataItem.Loader -> ITEM_VIEW_TYPE_LOADER
            else -> TODO()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> EventViewHolder.from(parent, glide)
            ITEM_VIEW_TYPE_LOADER -> LoaderViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EventViewHolder -> {
                val eventItem = getItem(position) as DataItem.EventItem
                holder.bind(eventItem.event, detailsClickListener, joinClickListener, anotherDateClickListener)
            }
        }
    }

    fun updateDataSourceByEvent(event: Event) {
        adapterScope.launch {
            val submittedList= currentList.map { if (it.id == event.id) DataItem.EventItem(event) else it }.toMutableList()

            withContext(Dispatchers.Main) {
                submitList(submittedList)
            }
        }
    }

    fun addLoaderToList() {
        adapterScope.launch {
            val submittedList= currentList.toMutableList()
            submittedList.add(DataItem.Loader)

            withContext(Dispatchers.Main) {
                submitList(submittedList)
            }
        }
    }

    fun removeLoaderFromList() {
        adapterScope.launch {
            val submittedList= currentList.toMutableList()
            submittedList.removeAt(currentList.size - 1)

            withContext(Dispatchers.Main) {
                submitList(submittedList)
            }
            EventBus.getDefault().post(NavigationCode.LOAD_MORE_HAS_ENDED)
        }
    }

    fun addHeaderAndSubmitList(list: List<Event>?) {
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
}