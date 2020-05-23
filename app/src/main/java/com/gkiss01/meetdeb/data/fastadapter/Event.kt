package com.gkiss01.meetdeb.data.fastadapter

import android.view.View
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.EventViewHolder
import com.gkiss01.meetdeb.utils.isActiveUserAdmin
import com.mikepenz.fastadapter.items.AbstractItem
import org.threeten.bp.OffsetDateTime
import java.io.Serializable

data class Event(
    val id: Long,
    val username: String,
    val userId: Long,
    var name: String,
    @com.gkiss01.meetdeb.adapter.OffsetDateTime
    var date: OffsetDateTime,
    var venue: String,
    var description: String,
    var reported: Boolean,
    val participants: Int,
    val accepted: Boolean,
    val voted: Boolean): Serializable, AbstractItem<EventViewHolder>() {

    constructor(name: String, date: OffsetDateTime, venue: String, description: String):
            this(Long.MIN_VALUE, "", Long.MIN_VALUE, name, date, venue, description, false, 0, false,  false)

    override val layoutRes: Int
        get() = R.layout.item_event
    override val type: Int
        get() = R.id.eli_layout
    override var identifier: Long
        get() = id
        set(_) {}

    override fun getViewHolder(v: View): EventViewHolder {
        return EventViewHolder(v, isActiveUserAdmin())
    }
}