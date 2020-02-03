package com.gkiss01.meetdeb.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.gkiss01.meetdeb.data.Date
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.data.Participant

class AdapterDiffCallback: DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

class AdapterClickListener(val clickListener: (position: Int) -> Unit) {
    fun onClick(position: Int) = clickListener(position)
}

sealed class DataItem {
    abstract val id: Long
    open val type: Types = Types.DATA_ITEM_TYPE_UNKNOWN

    data class EventItem(val event: Event): DataItem() {
        override val id = event.id
    }

    data class DateItem(val date: Date): DataItem() {
        override val id = date.id
    }

    data class ParticipantItem(val participant: Participant): DataItem() {
        override val id = participant.userId
    }

    object Header: DataItem() {
        override val id = Long.MIN_VALUE
    }

    object Loader: DataItem() {
        override val id = Long.MIN_VALUE + 1
        override val type = Types.DATA_ITEM_TYPE_LOADER
    }

    object Addition: DataItem() {
        override val id = Long.MIN_VALUE + 2
    }

    enum class Types {
        DATA_ITEM_TYPE_UNKNOWN,
        DATA_ITEM_TYPE_LOADER
    }
}