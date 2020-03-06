package com.gkiss01.meetdeb.adapter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.gkiss01.meetdeb.utils.updateOffsetDateTime
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

class DatePickerViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
    private var expanded = false
    private var offsetDateTime = OffsetDateTime.MIN
    val createButton: View = view.dla_createButton

    fun bind() {
        if (offsetDateTime == OffsetDateTime.MIN) {
            offsetDateTime = OffsetDateTime.now()
        }

        expanded = false
        view.dla_subLayout.visibility = View.GONE
        view.dla_dateTitle.text = offsetDateTime.format(dateFormatter)

        view.dla_headerLayout.setOnClickListener {
            view.dla_subLayout.visibility = if (expanded) View.GONE else View.VISIBLE
            view.dla_downArrow.animate().setDuration(200).rotation(if (expanded) 0F else 180F)
            expanded = !expanded
        }

        view.dla_dateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(view.dla_dateButton.context, DatePickerDialog.OnDateSetListener { _, year, monthValue, dayOfMonth ->
                offsetDateTime = updateOffsetDateTime(offsetDateTime, year, monthValue + 1, dayOfMonth)
                view.dla_dateTitle.text = offsetDateTime.format(dateFormatter)
            }, offsetDateTime.year, offsetDateTime.monthValue - 1, offsetDateTime.dayOfMonth)
            datePickerDialog.show()
        }

        val is24HourFormat = android.text.format.DateFormat.is24HourFormat(view.context)
        view.dla_timeButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(view.dla_timeButton.context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                offsetDateTime = updateOffsetDateTime(offsetDateTime, hourOfDay, minute)
                view.dla_dateTitle.text = offsetDateTime.format(dateFormatter)
            }, offsetDateTime.hour, offsetDateTime.minute, is24HourFormat)
            timePickerDialog.show()
        }
    }

    fun getPickedDate(): OffsetDateTime = offsetDateTime

    fun setError(error: String?) {
        view.dla_dateTitle.error = error
    }

    fun showDateCreateAnimation() {
        view.dla_createButton.showProgress {
            buttonTextRes = R.string.date_create_waiting
            progressColor = Color.WHITE
        }
    }

    fun clearData(closePicker: Boolean = false) {
        view.dla_createButton.hideProgress(R.string.date_create_button)
        if (closePicker) {
            view.dla_subLayout.visibility = View.GONE
            view.dla_downArrow.animate().setDuration(200).rotation(0F)
            expanded = false
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
    val voteButton: View = view.dli_voteButton

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

    fun showVoteCreateAnimation() {
        view.dli_voteButton.showProgress {
            progressColor = Color.parseColor("#485688")
        }
    }

    fun setRadioButtonUnchecked() {
        view.dli_voteButton.isChecked = false
    }
}

class EventViewHolder(private val view: View): FastAdapter.ViewHolder<Event>(view) {
    lateinit var event: Event
    val descButton: View = view.eli_descButton
    val joinButton: View = view.eli_acceptButton
    val anotherDateButton: View = view.eli_anotherDateButton

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
    }

    fun showEventJoinAnimation() {
        view.eli_acceptButton.showProgress {
            buttonTextRes = R.string.event_accept_waiting
            progressColor = Color.parseColor("#485688")
        }
    }

    fun showEventVoteAnimation() {
        view.eli_anotherDateButton.showProgress {
            buttonTextRes = R.string.event_accept_waiting
            progressColor = Color.parseColor("#485688")
        }
    }
}