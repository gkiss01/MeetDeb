package com.gkiss01.meetdeb.adapter

import android.graphics.Color
import android.view.View
import coil.load
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.fastadapter.Date
import com.gkiss01.meetdeb.data.fastadapter.DatePickerItem
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.data.fastadapter.Participant
import com.gkiss01.meetdeb.databinding.ItemDateBinding
import com.gkiss01.meetdeb.databinding.ItemDatePickerBinding
import com.gkiss01.meetdeb.databinding.ItemEventBinding
import com.gkiss01.meetdeb.databinding.ItemParticipantBinding
import com.gkiss01.meetdeb.network.BASE_URL
import com.gkiss01.meetdeb.utils.format
import com.mikepenz.fastadapter.FastAdapter
import org.threeten.bp.OffsetDateTime

class DatePickerViewHolder(view: View): FastAdapter.ViewHolder<DatePickerItem>(view) {
    val binding = ItemDatePickerBinding.bind(view)
    private var expanded = false

    override fun bindView(item: DatePickerItem, payloads: List<Any>) {
        expanded = false
        binding.subLayout.visibility = View.GONE
        updateSelectedDate(item.offsetDateTime)
    }

    override fun unbindView(item: DatePickerItem) {
        expanded = false
        binding.subLayout.visibility = View.GONE
        binding.dateLabel.text = null
    }

    fun updateSelectedDate(offsetDateTime: OffsetDateTime) {
        binding.dateLabel.text = offsetDateTime.format()
    }

    fun setError(error: String?) {
        binding.dateLabel.error = error
    }

    fun closeOrExpand() {
        expanded = !expanded
        binding.subLayout.visibility = if (expanded) View.VISIBLE else View.GONE
        binding.downArrow.animate().setDuration(200).rotation(if (expanded) 180F else 0F)
    }

    fun manageAnimation(show: Boolean) {
        if (show) showAnimation() else hideAnimation()
    }

    private fun showAnimation() {
        binding.createButton.showProgress {
            buttonTextRes = R.string.date_create_waiting
            progressColor = Color.WHITE
        }
    }

    private fun hideAnimation() {
        binding.createButton.hideProgress(R.string.date_create_button)
    }

    fun close() {
        expanded = true
        closeOrExpand()
    }

    fun expand() {
        expanded = false
        closeOrExpand()
    }
}

class ParticipantViewHolder(view: View): FastAdapter.ViewHolder<Participant>(view) {
    private val binding = ItemParticipantBinding.bind(view)

    override fun bindView(item: Participant, payloads: List<Any>) {
        binding.nameLabel.text = item.username
    }

    override fun unbindView(item: Participant) {
        binding.nameLabel.text = null
    }
}

class DateViewHolder(private val view: View): FastAdapter.ViewHolder<Date>(view) {
    val binding = ItemDateBinding.bind(view)
    lateinit var date: Date

    override fun bindView(item: Date, payloads: List<Any>) {
        date = item
        binding.dateLabel.text = item.date.format()
        binding.votesLabel.text = view.context.getString(R.string.event_votes, item.votes)
        binding.voteButton.isChecked = item.accepted

        binding.voteButton.hideProgress()
    }

    override fun unbindView(item: Date) {
        binding.dateLabel.text = null
        binding.votesLabel.text = null
        binding.voteButton.isChecked = false
    }

    fun showAnimation() {
        binding.voteButton.showProgress {
            progressColor = Color.parseColor("#485688")
        }
    }

    fun setChecked() {
        binding.voteButton.isChecked = true
    }
}

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