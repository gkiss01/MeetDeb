package com.gkiss01.meetdeb.data.remote.response

import android.view.View
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.screens.viewholders.ParticipantViewHolder
import com.mikepenz.fastadapter.items.AbstractItem

data class Participant(
    val eventId: Long?,
    val userId: Long,
    val username: String): AbstractItem<ParticipantViewHolder>() {

    override val layoutRes get() = R.layout.item_participant
    override val type get() = R.id.pli_layout
    override fun getViewHolder(v: View) = ParticipantViewHolder(v)
}