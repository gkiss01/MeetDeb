package com.gkiss01.meetdeb.data.fastadapter

import android.view.View
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.DateViewHolder
import com.mikepenz.fastadapter.items.AbstractItem
import org.threeten.bp.OffsetDateTime

data class Date(
    var id: Long,
    var eventId: Long,
    @com.gkiss01.meetdeb.adapter.OffsetDateTime
    var date: OffsetDateTime,
    var votes: Int,
    val accepted: Boolean): AbstractItem<DateViewHolder>() {

    override val layoutRes: Int
        get() = R.layout.item_date
    override val type: Int
        get() = R.id.dli_layout

    override fun getViewHolder(v: View): DateViewHolder {
        return DateViewHolder(v)
    }

}