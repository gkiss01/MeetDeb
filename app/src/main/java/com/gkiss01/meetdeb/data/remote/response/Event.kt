package com.gkiss01.meetdeb.data.remote.response

import android.view.View
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.remote.request.EventRequest
import com.gkiss01.meetdeb.screens.viewholders.EventViewHolder
import com.gkiss01.meetdeb.utils.classes.OffsetDateTimeCustom
import com.mikepenz.fastadapter.items.AbstractItem
import org.threeten.bp.OffsetDateTime
import java.io.Serializable

data class Event(
    val id: Long,
    val username: String,
    val userId: Long,
    var name: String,
    @OffsetDateTimeCustom
    var date: OffsetDateTime,
    var venue: String,
    var description: String,
    var reported: Boolean,
    val participants: Int,
    val accepted: Boolean,
    val voted: Boolean): Serializable, AbstractItem<EventViewHolder>() {

    constructor():
            this(Long.MIN_VALUE, "", Long.MIN_VALUE, "", OffsetDateTime.now(), "", "", false, 0, false,  false)
    constructor(name: String, date: OffsetDateTime, venue: String, description: String):
            this(Long.MIN_VALUE, "", Long.MIN_VALUE, name, date, venue, description, false, 0, false,  false)

    override val layoutRes get() = R.layout.item_event
    override val type get() = R.id.eli_layout
    override var identifier: Long
        get() = id
        set(_) {}

    override fun getViewHolder(v: View) = EventViewHolder(v,  MainActivity.instance.getActiveUser()?.isAdmin() ?: false)

    enum class UpdatingType {
        VOTE, PARTICIPATION, NONE
    }

    fun asRequest(): EventRequest {
        return EventRequest(id, name, date, venue, description)
    }
}