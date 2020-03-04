package com.gkiss01.meetdeb.adapter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.isProgressActive
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.MainActivity
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
import org.threeten.bp.ZoneOffset

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

class AdditionViewHolder(private val view: View, private val eventId: Long): RecyclerView.ViewHolder(view) {
    private var expanded = false
    private var year: Int = -1
    private var month: Int = -1
    private var day: Int = -1
    private var hour: Int = -1
    private var minute: Int = -1
    private var zoneOffset: ZoneOffset = ZoneOffset.UTC

    fun bind() {
        if (year == -1) {
            val date = OffsetDateTime.now()

            year = date.year
            month = date.monthValue
            day = date.dayOfMonth
            hour = date.hour
            minute = date.minute
            zoneOffset = date.offset
        }

        expanded = false
        view.dla_subLayout.visibility = View.GONE
        view.dla_dateTitle.text = OffsetDateTime.of(year, month, day, hour, minute, 0, 0, zoneOffset).format(
            dateFormatter)

        view.dla_layout.setOnClickListener {
            view.dla_subLayout.visibility = if (expanded) View.GONE else View.VISIBLE
            view.dla_downArrow.animate().setDuration(200).rotation(if (expanded) 0F else 180F)
            expanded = !expanded
        }

        view.dla_dateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(view.dla_dateButton.context, DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, dayOfMonth ->
                year = selectedYear
                month = selectedMonth + 1
                day = dayOfMonth
                view.dla_dateTitle.text = OffsetDateTime.of(year, month, day, hour, minute, 0, 0, zoneOffset).format(
                dateFormatter)

            }, year, month - 1, day)
            datePickerDialog.show()
        }

        val is24HourFormat = android.text.format.DateFormat.is24HourFormat(view.dla_timeButton.context)
        view.dla_timeButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(view.dla_timeButton.context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, selectedMinute ->
                hour = hourOfDay
                minute = selectedMinute
                view.dla_dateTitle.text = OffsetDateTime.of(year, month, day, hour, minute, 0, 0, zoneOffset).format(
                    dateFormatter)

            }, hour, minute, is24HourFormat)
            timePickerDialog.show()
        }

        view.dla_createButton.setOnClickListener {
            val date = OffsetDateTime.of(year, month, day, hour, minute, 0, 0, zoneOffset)
            if (date.isBefore(OffsetDateTime.now())) {
                view.dla_dateTitle.error = "Jövőbeli dátumot adj meg!"
            }
            else {
                view.dla_dateTitle.error = null
                view.dla_createButton.showProgress {
                    buttonTextRes = R.string.date_create_waiting
                    progressColor = Color.WHITE
                }
                MainActivity.instance.createDate(eventId, date)
            }
        }
    }

    fun clearData(deep: Boolean = false) {
        view.dla_createButton.hideProgress(R.string.date_create_button)
        if (deep) {
            view.dla_subLayout.visibility = View.GONE
            view.dla_downArrow.animate().setDuration(200).rotation(0F)
            expanded = false
        }
    }

    fun isProgressActive(): Boolean {
        return view.dla_createButton.isProgressActive()
    }

    companion object {
        fun from(parent: ViewGroup, eventId: Long): AdditionViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.dates_list_addition, parent, false)
            view.dla_createButton.attachTextChangeAnimator()
            return AdditionViewHolder(view, eventId)
        }
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