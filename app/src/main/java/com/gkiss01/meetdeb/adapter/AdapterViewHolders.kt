package com.gkiss01.meetdeb.adapter

import android.graphics.Color
import android.view.View
import coil.load
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.fastadapter.*
import com.gkiss01.meetdeb.network.BASE_URL
import com.mikepenz.fastadapter.FastAdapter
import kotlinx.android.synthetic.main.item_date.view.*
import kotlinx.android.synthetic.main.item_date_picker.view.*
import kotlinx.android.synthetic.main.item_event.view.*
import kotlinx.android.synthetic.main.item_participant.view.*
import org.threeten.bp.OffsetDateTime

class DatePickerViewHolder(private val view: View): FastAdapter.ViewHolder<DatePickerItem>(view) {
    private var expanded = false

    override fun bindView(item: DatePickerItem, payloads: List<Any>) {
        expanded = false
        view.dlp_subLayout.visibility = View.GONE
        updateSelectedDate(item.offsetDateTime)
    }

    override fun unbindView(item: DatePickerItem) {
        expanded = false
        view.dlp_subLayout.visibility = View.GONE
        view.dlp_dateTitle.text = null
    }

    fun updateSelectedDate(offsetDateTime: OffsetDateTime) {
        view.dlp_dateTitle.text = offsetDateTime.format()
    }

    fun setError(error: String?) {
        view.dlp_dateTitle.error = error
    }

    fun closeOrExpand() {
        expanded = !expanded
        view.dlp_subLayout.visibility = if (expanded) View.VISIBLE else View.GONE
        view.dlp_downArrow.animate().setDuration(200).rotation(if (expanded) 180F else 0F)
    }

    fun manageAnimation(show: Boolean) {
        if (show) showAnimation() else hideAnimation()
    }

    private fun showAnimation() {
        view.dlp_createButton.showProgress {
            buttonTextRes = R.string.date_create_waiting
            progressColor = Color.WHITE
        }
    }

    private fun hideAnimation() {
        view.dlp_createButton.hideProgress(R.string.date_create_button)
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

class ParticipantViewHolder(private val view: View): FastAdapter.ViewHolder<Participant>(view) {
    override fun bindView(item: Participant, payloads: List<Any>) {
        view.pli_name.text = item.username
    }

    override fun unbindView(item: Participant) {
        view.pli_name.text = null
    }
}

class DateViewHolder(private val view: View): FastAdapter.ViewHolder<Date>(view) {
    lateinit var date: Date

    override fun bindView(item: Date, payloads: List<Any>) {
        date = item
        view.dli_dateValue.text = item.date.format()
        view.dli_votes.text = view.context.getString(R.string.event_votes, item.votes)
        view.dli_voteButton.isChecked = item.accepted

        view.dli_voteButton.hideProgress()
    }

    override fun unbindView(item: Date) {
        view.dli_dateValue.text = null
        view.dli_votes.text = null
        view.dli_voteButton.isChecked = false
    }

    fun showAnimation() {
        view.dli_voteButton.showProgress {
            progressColor = Color.parseColor("#485688")
        }
    }

    fun setChecked() {
        view.dli_voteButton.isChecked = true
    }
}

class EventViewHolder(private val view: View, private val isAdmin: Boolean): FastAdapter.ViewHolder<Event>(view) {
    lateinit var event: Event

    override fun bindView(item: Event, payloads: List<Any>) {
        event = item
        view.eli_eventLabel.text = item.name
        view.eli_creatorLabel.text = item.username

        if (isAdmin)
            view.eli_creatorLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_creator, 0, if (item.reported) R.drawable.ic_report else 0, 0)

        if (item.accepted) {
            view.eli_acceptButton.hideProgress(R.string.event_accepted)
            view.eli_acceptButton.setBackgroundResource(R.drawable.event_button_accepted_background)
        }
        else {
            view.eli_acceptButton.hideProgress(R.string.event_not_accepted)
            view.eli_acceptButton.setBackgroundResource(0)
        }

        view.eli_anotherDateButton.hideProgress(R.string.event_date_add)
        view.eli_anotherDateButton.setBackgroundResource(if (item.voted) R.drawable.event_button_accepted_background else 0)

        view.eli_eventImage.load("$BASE_URL/images/${event.id}") {
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }
    }

    override fun unbindView(item: Event) {
        view.eli_eventLabel.text = null
        view.eli_acceptButton.hideProgress(R.string.event_not_accepted)
        view.eli_acceptButton.setBackgroundResource(0)
        view.eli_anotherDateButton.hideProgress(R.string.event_date_add)
        view.eli_anotherDateButton.setBackgroundResource(0)
    }

    fun manageAnimation(type: Event.UpdatingType) = when(type) {
        Event.UpdatingType.VOTE -> showVoteAnimation()
        Event.UpdatingType.PARTICIPATION -> showJoinAnimation()
    }

    private fun showVoteAnimation() {
        view.eli_anotherDateButton.showProgress {
            buttonTextRes = R.string.event_accept_waiting
            progressColor = Color.parseColor("#485688")
        }
    }

    private fun showJoinAnimation() {
        view.eli_acceptButton.showProgress {
            buttonTextRes = R.string.event_accept_waiting
            progressColor = Color.parseColor("#485688")
        }
    }
}