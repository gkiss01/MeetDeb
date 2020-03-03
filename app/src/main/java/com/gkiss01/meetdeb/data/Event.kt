package com.gkiss01.meetdeb.data

import android.view.View
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.EventViewHolder
import com.mikepenz.fastadapter.items.AbstractItem
import org.threeten.bp.OffsetDateTime
import java.io.Serializable

data class Event(
    var id: Long,
    var username: String,
    var name: String,
    @com.gkiss01.meetdeb.adapter.OffsetDateTime
    var date: OffsetDateTime,
    var venue: String,
    var labels: String,
    var participants: Int,
    val accepted: Boolean,
    val voted: Boolean): Serializable, AbstractItem<EventViewHolder>() {

    override val layoutRes: Int
        get() = R.layout.events_list_item
    override val type: Int
        get() = R.id.eli_eventCard
    override var identifier: Long
        get() = id
        set(value) {}

    override fun getViewHolder(v: View): EventViewHolder {
        return EventViewHolder(v)
    }
}