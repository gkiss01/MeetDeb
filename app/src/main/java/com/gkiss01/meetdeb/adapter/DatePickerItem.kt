package com.gkiss01.meetdeb.adapter

import android.view.View
import com.gkiss01.meetdeb.R
import com.mikepenz.fastadapter.items.AbstractItem

class DatePickerItem: AbstractItem<DatePickerViewHolder>() {
    override val type: Int
        get() = R.id.dla_layout
    override val layoutRes: Int
        get() = R.layout.dates_list_addition

    override fun bindView(holder: DatePickerViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)
        holder.bind()
    }

    override fun getViewHolder(v: View): DatePickerViewHolder = DatePickerViewHolder(v)
}