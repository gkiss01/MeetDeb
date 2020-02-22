package com.gkiss01.meetdeb.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.AdapterClickListener
import com.gkiss01.meetdeb.adapter.EventEntryAdapter
import com.gkiss01.meetdeb.adapter.EventViewHolder
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.data.EventList
import com.gkiss01.meetdeb.data.UpdateEventRequest
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import kotlinx.android.synthetic.main.events_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.system.exitProcess

class EventsFragment : Fragment() {

    private lateinit var viewModel: EventsViewModel
    private lateinit var viewAdapter: EventEntryAdapter

    private var selectedEventPosition = -1

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventsReceived(events: EventList) {
        viewModel.addEvents(events.events)
        ef_swipeRefreshLayout.isRefreshing = false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventReceived(event: Event) {
        viewAdapter.updateDataSourceByEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateRequestReceived(updateEventRequest: UpdateEventRequest) {
        val view = ef_eventsRecyclerView.findViewHolderForAdapterPosition(updateEventRequest.adapterPosition) as EventViewHolder
        if (!view.event.voted) {
            MainActivity.instance.getEvent(updateEventRequest.eventId)
            view.showEventVoteAnimation()
            selectedEventPosition = updateEventRequest.adapterPosition
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onErrorReceived(errorCode: ErrorCodes) {
        if (errorCode == ErrorCodes.UNKNOWN || errorCode == ErrorCodes.NO_EVENTS_FOUND) {
            if (selectedEventPosition != -1) {
                viewAdapter.notifyItemChanged(selectedEventPosition)
                selectedEventPosition = -1
            }
            if (viewModel.isMoreLoading.value == true) viewAdapter.removeLoaderFromList()
            ef_swipeRefreshLayout.isRefreshing = false
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationReceived(navigationCode: NavigationCode) {
        if (navigationCode == NavigationCode.LOAD_MORE_HAS_ENDED) {
            viewModel.isMoreLoading.value = false
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.events_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(EventsViewModel::class.java)

        ef_addActionButton.setOnClickListener{ run {
            val action = EventsFragmentDirections.actionEventsFragmentToCreateEventFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }}

        ef_swipeRefreshLayout.setOnRefreshListener {
            if (!viewModel.isMoreLoading.value!!)
                viewModel.refreshEvents()
            else
                ef_swipeRefreshLayout.isRefreshing = false
        }

        viewAdapter = EventEntryAdapter(AdapterClickListener { position ->
                val itemView = ef_eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventViewHolder
                val bottomSheet = DetailsBottomSheetFragment(itemView.event)
                bottomSheet.show(childFragmentManager, "DetailsBottomSheetFragment")
            },
            AdapterClickListener { position ->
                val itemView = ef_eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventViewHolder
                MainActivity.instance.modifyParticipation(itemView.event.id, itemView.event.accepted)
                itemView.showEventJoinAnimation()
                selectedEventPosition = position
            },
            AdapterClickListener { position ->
                val itemView = ef_eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventViewHolder
                val datesDialogFragment = DatesDialogFragment.newInstance(itemView.event.id, position)
                datesDialogFragment.show(childFragmentManager, "datesDialogFragment")
                MainActivity.instance.showDates(itemView.event.id)
        })
        viewAdapter.setHasStableIds(true)

        viewModel.events.observe(viewLifecycleOwner, Observer { events ->
            events?.let { viewAdapter.addHeaderAndSubmitList(it) }
        })

        val layoutManager = LinearLayoutManager(context)

        ef_eventsRecyclerView.adapter = viewAdapter
        ef_eventsRecyclerView.layoutManager = layoutManager
        ef_eventsRecyclerView.setHasFixedSize(true)
        ef_eventsRecyclerView.setItemViewCacheSize(20)

        ef_eventsRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0 && !viewModel.isMoreLoading.value!!) {
                    if (layoutManager.itemCount <= (layoutManager.findLastVisibleItemPosition() + 1)) {
                        viewModel.loadMoreEvents()
                        viewAdapter.addLoaderToList()
                    }
                }
            }
        })
    }
}