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
import com.mikepenz.fastadapter.FastAdapter

class EventViewHolder(view: View, private val isAdmin: Boolean): FastAdapter.ViewHolder<Event>(view) {
    val binding = ItemEventBinding.bind(view)
    lateinit var event: Event

    override fun bindView(item: Event, payloads: List<Any>) {
        event = item
        binding.eventLabel.text = item.name
        binding.creatorLabel.text = item.username

        if (isAdmin)
            binding.creatorLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_creator, 0, if (item.reported) R.drawable.ic_report else 0, 0)

        if (item.accepted) {
            binding.acceptButton.hideProgress(R.string.event_accepted)
            binding.acceptButton.setBackgroundResource(R.drawable.event_button_accepted_background)
        }
        else {
            binding.acceptButton.hideProgress(R.string.event_not_accepted)
            binding.acceptButton.setBackgroundResource(0)
        }

        binding.anotherDateButton.hideProgress(R.string.event_date_add)
        binding.anotherDateButton.setBackgroundResource(if (item.voted) R.drawable.event_button_accepted_background else 0)

        binding.eventImage.load("$BASE_URL/images/${event.id}") {
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }
    }

    override fun unbindView(item: Event) {
        binding.eventLabel.text = null
        binding.acceptButton.hideProgress(R.string.event_not_accepted)
        binding.acceptButton.setBackgroundResource(0)
        binding.anotherDateButton.hideProgress(R.string.event_date_add)
        binding.anotherDateButton.setBackgroundResource(0)
    }

    fun manageAnimation(type: Event.UpdatingType) = when(type) {
        Event.UpdatingType.VOTE -> showVoteAnimation()
        Event.UpdatingType.PARTICIPATION -> showJoinAnimation()
    }

    private fun showVoteAnimation() {
        binding.anotherDateButton.showProgress {
            buttonTextRes = R.string.event_accept_waiting
            progressColor = Color.parseColor("#485688")
        }
    }

    private fun showJoinAnimation() {
        binding.acceptButton.showProgress {
            buttonTextRes = R.string.event_accept_waiting
            progressColor = Color.parseColor("#485688")
        }
    }
}