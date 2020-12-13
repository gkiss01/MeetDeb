package com.gkiss01.meetdeb.screens.viewholders

import android.view.View
import com.gkiss01.meetdeb.data.remote.response.Participant
import com.gkiss01.meetdeb.databinding.ItemParticipantBinding
import com.mikepenz.fastadapter.FastAdapter

class ParticipantViewHolder(view: View): FastAdapter.ViewHolder<Participant>(view) {
    private val binding = ItemParticipantBinding.bind(view)

    override fun bindView(item: Participant, payloads: List<Any>) {
        binding.nameLabel.text = item.username
    }

    override fun unbindView(item: Participant) {
        binding.nameLabel.text = null
    }
}