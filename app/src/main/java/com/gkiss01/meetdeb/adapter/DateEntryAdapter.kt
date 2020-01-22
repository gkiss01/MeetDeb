package com.gkiss01.meetdeb.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.data.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ITEM_VIEW_TYPE_DATE = 0
private const val ITEM_VIEW_TYPE_ADDITION = 1
private const val ITEM_VIEW_TYPE_LOADER = 2

class DateEntryAdapter(private val detailsClickListener: AdapterClickListener): ListAdapter<DataItem, RecyclerView.ViewHolder>(AdapterDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Addition -> ITEM_VIEW_TYPE_ADDITION
            is DataItem.DateItem -> ITEM_VIEW_TYPE_DATE
            is DataItem.Loader -> ITEM_VIEW_TYPE_LOADER
            else -> TODO()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_ADDITION -> AdditionViewHolder.from(parent)
            ITEM_VIEW_TYPE_DATE -> DateViewHolder.from(parent)
            ITEM_VIEW_TYPE_LOADER -> LoaderViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DateViewHolder -> {
                val dateItem = getItem(position) as DataItem.DateItem
                holder.bind(dateItem.date, detailsClickListener)
            }
        }
    }

    fun addLoadingAndAddition() {
        adapterScope.launch {
            val items = listOf(DataItem.Loader, DataItem.Addition)

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    fun addAdditionAndSubmitList(list: List<Date>) {
        adapterScope.launch {
            val items =  list.map { DataItem.DateItem(it) } + listOf(DataItem.Addition)

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }
}
