package com.gkiss01.meetdeb.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.core.animation.addListener
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.databinding.EventsListItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.hypot
import kotlin.math.max

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class EventEntryAdapter(private val detailsClickListener: EventClickListener,
                        private val joinClickListener: EventClickListener): ListAdapter<DataItem, RecyclerView.ViewHolder>(EventEntryDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.EventItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> EntryViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EntryViewHolder -> {
                val eventItem = getItem(position) as DataItem.EventItem
                holder.bind(eventItem.event, detailsClickListener, joinClickListener)
            }
        }
    }

    fun updateDataSourceAtIndex(position: Int, event: Event) {
        adapterScope.launch {
            val submittedList = currentList.toMutableList()
            submittedList[position] = DataItem.EventItem(event)
            withContext(Dispatchers.Main) {
                submitList(submittedList)
            }
        }
    }

    fun addHeaderAndSubmitList(list: List<Event>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.EventItem(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    class HeaderViewHolder(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.events_list_header, parent, false) as View
                return HeaderViewHolder(itemView)
            }
        }
    }

    class EntryViewHolder(private val binding: EventsListItemBinding): RecyclerView.ViewHolder(binding.root) {
        var eventId = 0L
        var eventAccepted = false
        private var showDetails = false

        fun bind(item: Event, detailsClickListener: EventClickListener, joinClickListener: EventClickListener) {
            binding.event = item
            binding.descButton.setOnClickListener {
                detailsClickListener.onClick(this.layoutPosition)
            }
            binding.acceptButton.setOnClickListener {
                joinClickListener.onClick(this.layoutPosition)
            }

            binding.eventDetails.visibility = View.GONE
            binding.acceptCheck.visibility = if (item.accepted) View.VISIBLE else View.GONE

            eventId = item.id
            eventAccepted = item.accepted
            showDetails = false

            binding.executePendingBindings()
        }

        fun showEventDetails() {
            val cx = binding.descButton.x + binding.descButton.width / 2
            val cy = binding.descButton.y + binding.descButton.height / 2

            if (!showDetails) {
                var finalRadius = hypot(binding.eventDetails.width.toDouble(), binding.eventDetails.height.toDouble()).toFloat()
                if (finalRadius.equals(0f)) finalRadius = hypot(binding.eventImage.width.toDouble(), binding.eventImage.height.toDouble()).toFloat()
                val anim = ViewAnimationUtils.createCircularReveal(binding.eventDetails, cx.toInt(), cy.toInt(), 0f, finalRadius)
                anim.duration = 400L
                binding.eventDetails.visibility = View.VISIBLE

                anim.start()
                showDetails = true
            }
            else {
                val initialRadius = max(binding.eventDetails.width.toDouble(), binding.eventDetails.height.toDouble()).toFloat()
                val anim = ViewAnimationUtils.createCircularReveal(binding.eventDetails, cx.toInt(), cy.toInt(), initialRadius, 0f)
                anim.addListener(onEnd = {
                    binding.eventDetails.visibility = View.GONE
                })

                anim.start()
                showDetails = false
            }
        }

//        fun showEventJoin(accepted: Boolean) {
//            eventAccepted = accepted
//            binding.acceptButtonContainer.post {
//                TransitionManager.beginDelayedTransition(binding.acceptButtonContainer)
//                binding.acceptCheck.visibility = if (eventAccepted) View.VISIBLE else View.GONE
//            }
//
//        }

        companion object {
            fun from(parent: ViewGroup): EntryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = EventsListItemBinding.inflate(layoutInflater, parent, false)
                return EntryViewHolder(binding)
            }
        }
    }
}

sealed class DataItem {
    abstract val id: Long

    data class EventItem(var event: Event): DataItem() {
        override val id = event.id
    }

    object Header: DataItem() {
        override val id = Long.MIN_VALUE
    }
}

class EventClickListener(val clickListener: (position: Int) -> Unit) {
    fun onClick(position: Int) = clickListener(position)
}
