package com.gkiss01.meetdeb.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.network.NavigationCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

class EventEntryAdapter(private val detailsClickListener: AdapterClickListener,
                        private val joinClickListener: AdapterClickListener,
                        private val anotherDateClickListener: AdapterClickListener): AdapterClass() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewTypes.ITEM_VIEW_TYPE_EVENT.ordinal -> EventViewHolder.from(parent)
            ViewTypes.ITEM_VIEW_TYPE_HEADER.ordinal -> HeaderViewHolder.from(parent)
            ViewTypes.ITEM_VIEW_TYPE_LOADER.ordinal -> LoaderViewHolder.from(parent)
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

    override fun getItemId(position: Int): Long {
        return currentList[position].id.hashCode().toLong()
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
            if (submittedList.size > 0) submittedList.removeAt(currentList.size - 1)

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
            EventBus.getDefault().post(NavigationCode.LOAD_MORE_HAS_ENDED)
        }
    }

    public override fun getItem(position: Int): DataItem {
        return super.getItem(position)
    }
}