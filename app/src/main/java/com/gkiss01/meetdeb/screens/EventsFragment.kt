package com.gkiss01.meetdeb.screens

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.EventViewHolder
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.data.EventList
import com.gkiss01.meetdeb.data.UpdateEventRequest
import com.gkiss01.meetdeb.network.ErrorCodes
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericFastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.ui.items.ProgressItem
import kotlinx.android.synthetic.main.events_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.system.exitProcess

class EventsFragment : Fragment(R.layout.events_fragment) {
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
    fun onUpdateRequestReceived(request: UpdateEventRequest) {
        val position = itemAdapter.getAdapterPosition(request.eventId)
        val view = ef_eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventViewHolder
        Log.e("asd", "$view")
        if (!view.event.voted) {
            view.showEventVoteAnimation()
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
            exitProcess(0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ef_addActionButton.setOnClickListener{ run {
            val action = EventsFragmentDirections.actionEventsFragmentToCreateEventFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }}

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
        ef_eventsRecyclerView.setItemViewCacheSize(20)
        ef_eventsRecyclerView.itemAnimator = null

        viewModel.events.observe(viewLifecycleOwner, Observer { events ->
            events?.let { FastAdapterDiffUtil.set(itemAdapter, it) }
            ef_swipeRefreshLayout.isRefreshing = false
            viewModel.isMoreLoading = false // félős
            footerAdapter.clear()
        })

        fastAdapter.addEventHook(object : ClickEventHook<Event>() {
            override fun onBindMany(viewHolder: RecyclerView.ViewHolder): List<View>? {
                return if (viewHolder is EventViewHolder) listOf(viewHolder.descButton, viewHolder.joinButton, viewHolder.anotherDateButton)
                else null
            }

            override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<Event>, item: Event) {
                when (v.id) {
                    R.id.eli_descButton -> {
                        val bottomSheet = DetailsBottomSheetFragment(item)
                        bottomSheet.show(childFragmentManager, "DetailsBottomSheetFragment")
                    }
                    R.id.eli_acceptButton -> {
                        MainActivity.instance.modifyParticipation(item.id, item.accepted)

                        val itemView = ef_eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventViewHolder
                        itemView.showEventJoinAnimation()
                    }
                    R.id.eli_anotherDateButton -> {
                        MainActivity.instance.showDates(item.id)

                        val datesDialogFragment = DatesDialogFragment.newInstance(item.id, position)
                        datesDialogFragment.show(childFragmentManager, "datesDialogFragment")
                    }
                }
            }
        })

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
}