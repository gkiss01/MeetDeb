package com.gkiss01.meetdeb.data.fastadapter

import android.view.View
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.screens.viewholders.ParticipantViewHolder
import com.mikepenz.fastadapter.items.AbstractItem

data class Participant(
    val eventId: Long?,
    val userId: Long,
    val username: String): AbstractItem<ParticipantViewHolder>() {

    override val layoutRes: Int
        get() = R.layout.item_participant
    override val type: Int
        get() = R.id.pli_layout

    override fun getViewHolder(v: View): ParticipantViewHolder {
        return ParticipantViewHolder(v)
    }
}