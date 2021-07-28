package com.gkiss01.meetdeb.screens.viewholders

import android.graphics.Color
import android.view.View
import coil.load
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.remote.response.Event
import com.gkiss01.meetdeb.databinding.ItemEventBinding
import com.gkiss01.meetdeb.network.common.BASE_URL
import com.gkiss01.meetdeb.viewmodels.ItemUpdating
import com.mikepenz.fastadapter.FastAdapter

class EventViewHolder(view: View, private val isAdmin: Boolean): FastAdapter.ViewHolder<Event>(view) {
    val binding = ItemEventBinding.bind(view)

    override fun bindView(item: Event, payloads: List<Any>) {
        binding.eventLabel.text = item.name
        binding.creatorLabel.text = item.username

        binding.eventImage.load("$BASE_URL/images/${item.id}") {
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }

        if (isAdmin) {
            val drawable = if (item.reported) R.drawable.ic_report else 0
            binding.creatorLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_creator, 0, drawable, 0)
        }

        val acceptDrawable = if (item.accepted) R.drawable.event_button_accepted_background else 0
        val voteDrawable = if (item.voted) R.drawable.event_button_accepted_background else 0
        binding.acceptButton.setBackgroundResource(acceptDrawable)
        binding.anotherDateButton.setBackgroundResource(voteDrawable)

        payloads.filterIsInstance<ItemUpdating>().firstOrNull()?.let {
            if (item.id == it.second) manageAnimation(it.first, item) else manageAnimation(Event.UpdatingType.NONE, item)
        } ?: manageAnimation(Event.UpdatingType.NONE, item)
    }

    override fun unbindView(item: Event) {
        binding.eventLabel.text = null
        binding.creatorLabel.text = null
        binding.acceptButton.hideProgress(R.string.event_not_accepted)
        binding.acceptButton.setBackgroundResource(0)
        binding.anotherDateButton.hideProgress(R.string.event_date_add)
        binding.anotherDateButton.setBackgroundResource(0)
    }

    private fun manageAnimation(type: Event.UpdatingType, item: Event) = when(type) {
        Event.UpdatingType.VOTE -> showVoteAnimation(item)
        Event.UpdatingType.PARTICIPATION -> showAcceptAnimation(item)
        Event.UpdatingType.NONE -> clearAnimations(item)
    }

    private fun clearAnimations(item: Event) {
        binding.acceptButton.hideProgress(if (item.accepted) R.string.event_accepted else R.string.event_not_accepted)
        binding.anotherDateButton.hideProgress(R.string.event_date_add)
    }

    private fun showAcceptAnimation(@Suppress("UNUSED_PARAMETER") item: Event) {
        binding.anotherDateButton.hideProgress(R.string.event_date_add)
        binding.acceptButton.showProgress {
            buttonTextRes = R.string.event_accept_waiting
            progressColor = Color.parseColor("#485688")
        }
    }

    private fun showVoteAnimation(item: Event) {
        binding.acceptButton.hideProgress(if (item.accepted) R.string.event_accepted else R.string.event_not_accepted)
        binding.anotherDateButton.showProgress {
            buttonTextRes = R.string.event_accept_waiting
            progressColor = Color.parseColor("#485688")
        }
    }
}