package com.gkiss01.meetdeb.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.core.animation.addListener
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.databinding.EventsListItemBinding
import com.gkiss01.meetdeb.network.BASE_URL
import com.gkiss01.meetdeb.network.GlideRequests
import com.gkiss01.meetdeb.network.NavigationCode
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import kotlin.math.hypot
import kotlin.math.max

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1
private const val ITEM_VIEW_TYPE_LOADER = 2

class EventEntryAdapter(val glide: GlideRequests,
                        private val detailsClickListener: AdapterClickListener,
                        private val joinClickListener: AdapterClickListener,
                        private val anotherDateClickListener: AdapterClickListener): ListAdapter<DataItem, RecyclerView.ViewHolder>(AdapterDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.EventItem -> ITEM_VIEW_TYPE_ITEM
            is DataItem.Loader -> ITEM_VIEW_TYPE_LOADER
            else -> TODO()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> EventViewHolder.from(parent, glide)
            ITEM_VIEW_TYPE_LOADER -> LoaderViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EventViewHolder -> {
                val eventItem = getItem(position) as DataItem.EventItem
                holder.bind(eventItem.event, detailsClickListener, joinClickListener, anotherDateClickListener)
            }
        }
    }

    fun updateDataSourceByEvent(event: Event) {
        adapterScope.launch {
            val submittedList= currentList.map { if (it.id == event.id) DataItem.EventItem(event) else it }.toMutableList()

            withContext(Dispatchers.Main) {
                submitList(submittedList)
            }
        }
    }

    fun addLoaderToList() {
        adapterScope.launch {
            val submittedList= currentList.toMutableList()
            submittedList.add(DataItem.Loader)

            withContext(Dispatchers.Main) {
                submitList(submittedList)
            }
        }
    }

    fun removeLoaderFromList() {
        adapterScope.launch {
            val submittedList= currentList.toMutableList()
            submittedList.removeAt(currentList.size - 1)

            withContext(Dispatchers.Main) {
                submitList(submittedList)
            }
            EventBus.getDefault().post(NavigationCode.LOAD_MORE_HAS_ENDED)
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

    class LoaderViewHolder(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): LoaderViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.events_list_loader, parent, false) as View
                return LoaderViewHolder(itemView)
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
                Blurry.with(binding.eventImage.context).capture(binding.eventImage).into(binding.eventImage)

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

        companion object {
            fun from(parent: ViewGroup, glide: GlideRequests): EventViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = EventsListItemBinding.inflate(layoutInflater, parent, false)
                binding.acceptButton.attachTextChangeAnimator()
                return EventViewHolder(binding, glide)
            }
        }
    }
}
