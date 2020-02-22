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
import com.gkiss01.meetdeb.databinding.DatesListAdditionBinding
import com.gkiss01.meetdeb.databinding.DatesListItemBinding
import com.gkiss01.meetdeb.databinding.ParticipantsListItemBinding
import com.gkiss01.meetdeb.network.BASE_URL
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.events_list_item.view.*
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

class HeaderViewHolder(view: View): RecyclerView.ViewHolder(view) {
    companion object {
        fun from(parent: ViewGroup): HeaderViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.events_list_header, parent, false) as View
            return HeaderViewHolder(itemView)
        }
    }
}

class LoaderViewHolder(view: View): RecyclerView.ViewHolder(view) {
    companion object {
        fun from(parent: ViewGroup): LoaderViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.events_list_loader, parent, false) as View
            return LoaderViewHolder(itemView)
        }
    }
}

class AdditionViewHolder(private val binding: DatesListAdditionBinding, private val eventId: Long): RecyclerView.ViewHolder(binding.root) {
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

        binding.addDateLayout.visibility = View.GONE
        expanded = false

        binding.addLayout.setOnClickListener {
            binding.addDateLayout.visibility = if (expanded) View.GONE else View.VISIBLE
            binding.downArrow.animate().setDuration(200).rotation(if (expanded) 0F else 180F)
            expanded = !expanded
        }

        binding.dateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(binding.dateButton.context, DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, dayOfMonth ->

                year = selectedYear
                month = selectedMonth + 1
                day = dayOfMonth
                binding.eventDateTime.setDateFormat(OffsetDateTime.of(year, month, day, hour, minute, 0, 0, zoneOffset))

            }, year, month - 1, day)
            datePickerDialog.show()
        }

        val is24HourFormat = android.text.format.DateFormat.is24HourFormat(binding.timeButton.context)
        binding.timeButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(binding.dateButton.context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, selectedMinute ->

                hour = hourOfDay
                minute = selectedMinute
                binding.eventDateTime.setDateFormat(OffsetDateTime.of(year, month, day, hour, minute, 0, 0, zoneOffset))

            }, hour, minute, is24HourFormat)
            timePickerDialog.show()
        }

        binding.createButton.setOnClickListener {
            val date = OffsetDateTime.of(year, month, day, hour, minute, 0, 0, zoneOffset)
            if (date.isBefore(OffsetDateTime.now())) {
                binding.eventDateTime.error = "Jövőbeli dátumot adj meg!"
            }
            else {
                binding.eventDateTime.error = null
                binding.createButton.showProgress {
                    buttonTextRes = R.string.date_create_waiting
                    progressColor = Color.WHITE
                }
                MainActivity.instance.createDate(eventId, date)
            }
        }

        binding.eventDateTime.setDateFormat(OffsetDateTime.of(year, month, day, hour, minute, 0, 0, zoneOffset))
    }

    fun clearData(deep: Boolean = false) {
        binding.createButton.hideProgress(R.string.date_create_button)
        if (deep) {
            binding.addDateLayout.visibility = View.GONE
            binding.downArrow.animate().setDuration(200).rotation(0F)
            expanded = false
        }
    }

    fun isProgressActive(): Boolean {
        return binding.createButton.isProgressActive()
    }

    companion object {
        fun from(parent: ViewGroup, eventId: Long): AdditionViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = DatesListAdditionBinding.inflate(layoutInflater, parent, false)
            binding.createButton.attachTextChangeAnimator()
            return AdditionViewHolder(binding, eventId)
        }
    }
}

class DateViewHolder(private val binding: DatesListItemBinding): RecyclerView.ViewHolder(binding.root) {
    var dateId = 0L

    fun bind(item: Date, dateClickListener: AdapterClickListener) {
        dateId = item.id
        binding.date = item
        binding.voteButton.setOnClickListener {
            if (!item.accepted) dateClickListener.onClick(this.adapterPosition)
        }

        binding.voteButton.hideProgress()

        binding.executePendingBindings()
    }

    fun showVoteCreateAnimation() {
        binding.voteButton.showProgress {
            progressColor = Color.parseColor("#485688")
        }
    }

    fun setRadioButtonUnchecked() {
        binding.voteButton.isChecked = false
    }

    companion object {
        fun from(parent: ViewGroup): DateViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = DatesListItemBinding.inflate(layoutInflater, parent, false)
            return DateViewHolder(binding)
        }
    }
}

class ParticipantViewHolder(private val binding: ParticipantsListItemBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Participant) {
        binding.name.text = item.username
    }

    companion object {
        fun from(parent: ViewGroup): ParticipantViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ParticipantsListItemBinding.inflate(layoutInflater, parent, false)
            return ParticipantViewHolder(binding)
        }
    }
}

class EventViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
    lateinit var event: Event

    fun bind(item: Event, detailsClickListener: AdapterClickListener, joinClickListener: AdapterClickListener, anotherDateClickListener: AdapterClickListener) {
        event = item
        view.eli_eventLabel.text = item.name

        view.eli_descButton.setOnClickListener {
            detailsClickListener.onClick(this.adapterPosition)
        }
        view.eli_acceptButton.setOnClickListener {
            joinClickListener.onClick(this.adapterPosition)
        }
        view.eli_anotherDateButton.setOnClickListener {
            anotherDateClickListener.onClick(this.adapterPosition)
        }

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

    companion object {
        fun from(parent: ViewGroup): EventViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.events_list_item, parent, false)
            return EventViewHolder(view)
        }
    }
}