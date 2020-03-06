package com.gkiss01.meetdeb.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.isProgressActive
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.Date
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.data.Participant
import com.gkiss01.meetdeb.network.BASE_URL
import com.gkiss01.meetdeb.utils.dateFormatter
import com.mikepenz.fastadapter.FastAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dates_list_addition.view.*
import kotlinx.android.synthetic.main.dates_list_item.view.*
import kotlinx.android.synthetic.main.events_list_item.view.*
import kotlinx.android.synthetic.main.participants_list_item.view.*
import org.threeten.bp.OffsetDateTime

class HeaderViewHolder(view: View): RecyclerView.ViewHolder(view) {
    companion object {
        fun from(parent: ViewGroup): HeaderViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.events_list_header, parent, false) as View
            return HeaderViewHolder(view)
        }
    }
}

class LoaderViewHolder(view: View): RecyclerView.ViewHolder(view) {
    companion object {
        fun from(parent: ViewGroup): LoaderViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.events_list_loader, parent, false) as View
            return LoaderViewHolder(view)
        }
    }
}

class DatePickerViewHolder(private val view: View): FastAdapter.ViewHolder<DatePickerItem>(view) {
    private var expanded = false

    override fun bindView(item: DatePickerItem, payloads: List<Any>) {
        expanded = false
        view.dla_subLayout.visibility = View.GONE
        updateSelectedDate(item.offsetDateTime)
    }

    override fun unbindView(item: DatePickerItem) {
        expanded = false
        view.dla_subLayout.visibility = View.GONE
        view.dla_dateTitle.text = null
    }

    fun updateSelectedDate(offsetDateTime: OffsetDateTime) {
        view.dla_dateTitle.text = offsetDateTime.format(dateFormatter)
    }

    fun setError(error: String?) {
        view.dla_dateTitle.error = error
    }

    fun closeOrExpand() {
        expanded = !expanded
        view.dla_subLayout.visibility = if (expanded) View.VISIBLE else View.GONE
        view.dla_downArrow.animate().setDuration(200).rotation(if (expanded) 180F else 0F)
    }

    fun showAnimation() {
        view.dla_createButton.showProgress {
            buttonTextRes = R.string.date_create_waiting
            progressColor = Color.WHITE
        }
    }

    fun clearAnimation(closePicker: Boolean = false) {
        view.dla_createButton.hideProgress(R.string.date_create_button)
        if (closePicker) {
            expanded = false
            view.dla_subLayout.visibility = View.GONE
            view.dla_downArrow.animate().setDuration(200).rotation(0F)
        }
    }

    fun isProgressActive(): Boolean {
        return view.dla_createButton.isProgressActive()
    }
}

class ParticipantViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
    fun bind(item: Participant) {
        view.pli_name.text = item.username
    }

    companion object {
        fun from(parent: ViewGroup): ParticipantViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.participants_list_item, parent, false)
            return ParticipantViewHolder(view)
        }
    }
}

class DateViewHolder(private val view: View): FastAdapter.ViewHolder<Date>(view) {
    var dateId = Long.MIN_VALUE

    override fun bindView(item: Date, payloads: List<Any>) {
        dateId = item.id
        view.dli_dateValue.text = item.date.format(dateFormatter)
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