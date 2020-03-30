package com.gkiss01.meetdeb.adapter

import android.graphics.Color
import android.view.View
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.isProgressActive
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.fastadapter.Date
import com.gkiss01.meetdeb.data.fastadapter.DatePickerItem
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.data.fastadapter.Participant
import com.gkiss01.meetdeb.network.BASE_URL
import com.gkiss01.meetdeb.utils.formatDate
import com.gkiss01.meetdeb.utils.isActiveUserAdmin
import com.mikepenz.fastadapter.FastAdapter
import com.squareup.picasso.Picasso
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
        view.dlp_dateTitle.text = formatDate(offsetDateTime)
    }

    fun setError(error: String?) {
        view.dlp_dateTitle.error = error
    }

    fun closeOrExpand() {
        expanded = !expanded
        view.dlp_subLayout.visibility = if (expanded) View.VISIBLE else View.GONE
        view.dlp_downArrow.animate().setDuration(200).rotation(if (expanded) 180F else 0F)
    }

    fun showAnimation() {
        view.dlp_createButton.showProgress {
            buttonTextRes = R.string.date_create_waiting
            progressColor = Color.WHITE
        }
    }

    fun clearAnimation(closePicker: Boolean = false) {
        view.dlp_createButton.hideProgress(R.string.date_create_button)
        if (closePicker) {
            expanded = false
            view.dlp_subLayout.visibility = View.GONE
            view.dlp_downArrow.animate().setDuration(200).rotation(0F)
        }
    }

    fun isProgressActive(): Boolean {
        return view.dlp_createButton.isProgressActive()
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
    var dateId = Long.MIN_VALUE

    override fun bindView(item: Date, payloads: List<Any>) {
        dateId = item.id
        view.dli_dateValue.text = formatDate(item.date)
        view.dli_votes.text = "Szavazatok: ${item.votes}"
        view.dli_voteButton.isChecked = item.accepted

        view.dli_voteButton.hideProgress()
    }

    override fun unbindView(item: Date) {
        dateId = Long.MIN_VALUE
        view.dli_dateValue.text = null
        view.dli_votes.text = null
        view.dli_voteButton.isChecked = false
    }

    fun showAnimation() {
        view.dli_voteButton.showProgress {
            progressColor = Color.parseColor("#485688")
        }
    }

    fun setUnchecked() {
        view.dli_voteButton.isChecked = false
    }
}

class EventViewHolder(private val view: View): FastAdapter.ViewHolder<Event>(view) {
    lateinit var event: Event

    override fun bindView(item: Event, payloads: List<Any>) {
        event = item
        view.eli_eventLabel.text = item.name
        view.eli_creatorLabel.text = item.username

        if (isActiveUserAdmin())
            view.eli_creatorLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_creator, 0, if (item.reported) R.drawable.ic_report else 0, 0)

        if (item.accepted) {
            view.eli_acceptButton.hideProgress(R.string.event_accepted)
            view.eli_acceptButton.setBackgroundResource(R.drawable.event_accepted_button_background)
        }
        else {
            view.eli_acceptButton.hideProgress(R.string.event_not_accepted)
            view.eli_acceptButton.setBackgroundResource(0)
        }

        view.eli_anotherDateButton.hideProgress(R.string.event_date_add)
        view.eli_anotherDateButton.setBackgroundResource(if (item.voted) R.drawable.event_accepted_button_background else 0)

        Picasso.get()
            .load("$BASE_URL/images/${event.id}")
            .placeholder(R.drawable.placeholder)
            .into(view.eli_eventImage)
    }

    override fun unbindView(item: Event) {
        view.eli_eventLabel.text = null
        view.eli_acceptButton.hideProgress(R.string.event_not_accepted)
        view.eli_acceptButton.setBackgroundResource(0)
        view.eli_anotherDateButton.hideProgress(R.string.event_date_add)
        view.eli_anotherDateButton.setBackgroundResource(0)
    }

    fun showJoinAnimation() {
        view.eli_acceptButton.showProgress {
            buttonTextRes = R.string.event_accept_waiting
            progressColor = Color.parseColor("#485688")
        }
    }

    fun showVoteAnimation() {
        view.eli_anotherDateButton.showProgress {
            buttonTextRes = R.string.event_accept_waiting
            progressColor = Color.parseColor("#485688")
        }
    }
}