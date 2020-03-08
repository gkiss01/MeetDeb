package com.gkiss01.meetdeb.screens

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.EventViewHolder
import com.gkiss01.meetdeb.data.EventList
import com.gkiss01.meetdeb.data.adapterrequest.DeleteEventRequest
import com.gkiss01.meetdeb.data.adapterrequest.UpdateEventRequest
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.network.ErrorCodes
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericFastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.addClickListener
import com.mikepenz.fastadapter.ui.items.ProgressItem
import com.mikepenz.itemanimators.AlphaInAnimator
import kotlinx.android.synthetic.main.events_fragment.*
import kotlinx.android.synthetic.main.events_list_item.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class EventsFragment : Fragment(R.layout.events_fragment), PopupMenu.OnMenuItemClickListener {
    private val viewModel: EventsViewModel by activityViewModels()

    private val itemAdapter = ItemAdapter<Event>()
    private val footerAdapter = ItemAdapter<ProgressItem>()
    private lateinit var fastAdapter: GenericFastAdapter

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventReceived(event: Event) {
        viewModel.updateEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventsReceived(events: EventList) {
        viewModel.addEvents(events.events)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeleteRequestReceived(request: DeleteEventRequest) {
        viewModel.deleteEvent(request.eventId)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateRequestReceived(request: UpdateEventRequest) {
        val position = itemAdapter.getAdapterPosition(request.eventId)
        val view = ef_eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventViewHolder

        if (!view.event.voted) {
            view.showVoteAnimation()
            viewModel.selectedEvent = request.eventId

            MainActivity.instance.getEvent(request.eventId)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onErrorReceived(errorCode: ErrorCodes) {
        if (errorCode == ErrorCodes.UNKNOWN || errorCode == ErrorCodes.NO_EVENTS_FOUND) {
            if (viewModel.selectedEvent != Long.MIN_VALUE) {
                fastAdapter.notifyAdapterItemChanged(itemAdapter.getAdapterPosition(viewModel.selectedEvent))
                viewModel.selectedEvent = Long.MIN_VALUE
            }
            if (viewModel.isMoreLoading) {
                viewModel.isMoreLoading = false
                footerAdapter.clear()
            }
            ef_swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finishAffinity()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ef_addActionButton.setOnClickListener{ findNavController().navigate(R.id.createEventFragment) }

        ef_swipeRefreshLayout.setOnRefreshListener {
            if (!viewModel.isMoreLoading)
                viewModel.refreshEvents()
            else ef_swipeRefreshLayout.isRefreshing = false
        }

        fastAdapter = FastAdapter.with(listOf(itemAdapter, footerAdapter))
        fastAdapter.attachDefaultListeners = false
        ef_eventsRecyclerView.adapter = fastAdapter

        val layoutManager = LinearLayoutManager(context)
        ef_eventsRecyclerView.layoutManager = layoutManager

        ef_eventsRecyclerView.setHasFixedSize(true)
        ef_eventsRecyclerView.setItemViewCacheSize(6)
        ef_eventsRecyclerView.itemAnimator = AlphaInAnimator()

        viewModel.events.observe(viewLifecycleOwner, Observer {
            FastAdapterDiffUtil[itemAdapter] = it
            ef_eventsRecyclerView.scheduleLayoutAnimation()
            ef_swipeRefreshLayout.isRefreshing = false
            viewModel.isMoreLoading = false // félős
            footerAdapter.clear()
        })

        itemAdapter.fastAdapter!!.addClickListener( {null}, { vh: EventViewHolder -> listOf<View>(vh.itemView.eli_descButton, vh.itemView.eli_acceptButton, vh.itemView.eli_anotherDateButton, vh.itemView.eli_moreButton) }) { v, position, _, item ->
            when (v.id) {
                R.id.eli_descButton -> {
                    findNavController().navigate(EventsFragmentDirections.actionEventsFragmentToDetailsBottomSheetFragment(item))
                }
                R.id.eli_acceptButton -> {
                    MainActivity.instance.modifyParticipation(item.id, item.accepted)

                    val itemView = ef_eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventViewHolder
                    itemView.showJoinAnimation()
                }
                R.id.eli_anotherDateButton -> {
                    MainActivity.instance.showDates(item.id)

                    findNavController().navigate(EventsFragmentDirections.actionEventsFragmentToDatesDialogFragment(item.id))
                }
                R.id.eli_moreButton -> {
                    PopupMenu(context, v).apply {
                        setOnMenuItemClickListener(this@EventsFragment)
                        setOnDismissListener {
                            viewModel.selectedEvent = Long.MIN_VALUE
                        }
                        viewModel.selectedEvent = item.id
                        inflate(if (MainActivity.instance.isUserAdmin()) R.menu.event_more_admin else R.menu.event_more)
                        show()
                    }
                }
            }
        }

        ef_eventsRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0 && !viewModel.isMoreLoading) {
                    if (layoutManager.itemCount <= (layoutManager.findLastVisibleItemPosition() + 1)) {
                        footerAdapter.clear()
                        footerAdapter.add(ProgressItem())

                        viewModel.loadMoreEvents()
                    }
                }
            }
        })
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.report -> {
                MainActivity.instance.reportEvent(viewModel.selectedEvent)
                true
            }
            R.id.removeReport -> {
                MainActivity.instance.removeReport(viewModel.selectedEvent)
                true
            }
            R.id.delete -> {
                MainActivity.instance.deleteEvent(viewModel.selectedEvent)
                true
            }
            else -> false
        }
    }
}