package com.gkiss01.meetdeb.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.data.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DateEntryAdapter(private val detailsClickListener: AdapterClickListener): AdapterClass() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewTypes.ITEM_VIEW_TYPE_DATE.ordinal -> DateViewHolder.from(parent)
            ViewTypes.ITEM_VIEW_TYPE_LOADER.ordinal -> LoaderViewHolder.from(parent)
            ViewTypes.ITEM_VIEW_TYPE_ADDITION.ordinal -> AdditionViewHolder.from(parent)
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
