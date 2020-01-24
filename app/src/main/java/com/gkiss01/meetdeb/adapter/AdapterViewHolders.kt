package com.gkiss01.meetdeb.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.recyclerview.widget.RecyclerView
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.Date
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.databinding.DatesListItemBinding
import com.gkiss01.meetdeb.databinding.EventsListItemBinding
import com.gkiss01.meetdeb.network.BASE_URL
import com.gkiss01.meetdeb.network.GlideRequests
import com.skydoves.expandablelayout.ExpandableLayout
import com.skydoves.expandablelayout.expandableLayout
import com.vansuita.gaussianblur.GaussianBlur
import org.threeten.bp.OffsetDateTime
import kotlin.math.hypot
import kotlin.math.max

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

class AdditionViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
    fun bind() {
        val expandableLayout = view.findViewById<ExpandableLayout>(R.id.expandableLayout)
        expandableLayout.parentLayout.setOnClickListener {
            if (expandableLayout.isExpanded) expandableLayout.collapse()
            else expandableLayout.expand()
        }

        expandableLayout.secondLayout.findViewById<TextView>(R.id.eventDateTime).setDateFormat(OffsetDateTime.now())
    }

    companion object {
        fun from(parent: ViewGroup): AdditionViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.dates_list_addition, parent, false) as View
            return AdditionViewHolder(itemView)
        }
    }
}

class DateViewHolder(private val binding: DatesListItemBinding): RecyclerView.ViewHolder(binding.root) {
    var dateId = 0L

    fun bind(item: Date, dateClickListener: AdapterClickListener) {
        binding.date = item
        binding.voteButton.setOnClickListener {
            if (!item.accepted) dateClickListener.onClick(this.adapterPosition)
        }

        binding.voteButton.hideProgress()

        dateId = item.id

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

class EventViewHolder(private val binding: EventsListItemBinding, private val glide: GlideRequests): RecyclerView.ViewHolder(binding.root) {
    var eventId = 0L
    var eventAccepted = false
    private var showDetails = false

    fun bind(item: Event, detailsClickListener: AdapterClickListener, joinClickListener: AdapterClickListener, anotherDateClickListener: AdapterClickListener) {
        binding.event = item
        binding.descButton.setOnClickListener {
            detailsClickListener.onClick(this.adapterPosition)
        }
        binding.acceptButton.setOnClickListener {
            joinClickListener.onClick(this.adapterPosition)
        }
        binding.anotherDateButton.setOnClickListener {
            anotherDateClickListener.onClick(this.adapterPosition)
        }

        binding.eventDetails.visibility = View.GONE
        binding.eventLabel.visibility = View.VISIBLE
        if (item.accepted) {
            binding.acceptButton.hideProgress(R.string.event_accepted)
            binding.acceptButton.setBackgroundResource(R.drawable.event_accepted_button_background)
        }
        else {
            binding.acceptButton.hideProgress(R.string.event_not_accepted)
            binding.acceptButton.setBackgroundResource(0)
        }

        binding.anotherDateButton.hideProgress(R.string.event_another_date_button)
        binding.anotherDateButton.setBackgroundResource(if (item.voted) R.drawable.event_accepted_button_background else 0)

        eventId = item.id
        eventAccepted = item.accepted
        showDetails = false

        glide.load("$BASE_URL/images/$eventId")
            .into(binding.eventImage)

        binding.executePendingBindings()
    }

    fun showEventDetails() {
        val cx = binding.descButton.x + binding.descButton.width / 2
        val cy = binding.descButton.y + binding.descButton.height / 2

        if (!showDetails) {
            var finalRadius = hypot(binding.eventDetails.width.toDouble(), binding.eventDetails.height.toDouble()).toFloat()
            if (finalRadius.equals(0f))
                finalRadius = hypot(binding.eventImage.width.toDouble(), binding.eventImage.height.toDouble()).toFloat()

            val anim = ViewAnimationUtils.createCircularReveal(binding.eventDetails, cx.toInt(), cy.toInt(), 0f, finalRadius)
            anim.duration = 400L
            binding.eventDetails.visibility = View.VISIBLE
            binding.eventLabel.visibility = View.INVISIBLE
            GaussianBlur.with(binding.eventImage.context).put(binding.eventImage.drawable, binding.eventImage)

            anim.start()
            showDetails = true
        }
        else {
            val initialRadius = max(binding.eventDetails.width.toDouble(), binding.eventDetails.height.toDouble()).toFloat()
            val anim = ViewAnimationUtils.createCircularReveal(binding.eventDetails, cx.toInt(), cy.toInt(), initialRadius, 0f)
            anim.addListener(onEnd = {
                binding.eventDetails.visibility = View.GONE
                binding.eventLabel.visibility = View.VISIBLE
                glide.load("$BASE_URL/images/$eventId")
                    .into(binding.eventImage)
            })

            anim.start()
            showDetails = false
        }
    }

    fun showEventJoinAnimation() {
        binding.acceptButton.showProgress {
            buttonTextRes = R.string.event_accept_waiting
            progressColor = Color.parseColor("#485688")
        }
    }

    fun showEventVoteAnimation() {
        binding.anotherDateButton.showProgress {
            buttonTextRes = R.string.event_accept_waiting
            progressColor = Color.parseColor("#485688")
        }
    }

    companion object {
        fun from(parent: ViewGroup, glide: GlideRequests): EventViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = EventsListItemBinding.inflate(layoutInflater, parent, false)
            binding.acceptButton.attachTextChangeAnimator()
            binding.anotherDateButton.attachTextChangeAnimator()
            return EventViewHolder(binding, glide)
        }
    }
}