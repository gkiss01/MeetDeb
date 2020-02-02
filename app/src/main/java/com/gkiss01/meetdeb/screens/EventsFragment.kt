package com.gkiss01.meetdeb.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
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
import com.gkiss01.meetdeb.databinding.EventsFragmentBinding
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.system.exitProcess

class EventsFragment : Fragment() {

    private lateinit var binding: EventsFragmentBinding
    private lateinit var viewModel: EventsViewModel
    private lateinit var viewAdapter: EventEntryAdapter

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
        binding.swipeRefreshLayout.isRefreshing = false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventReceived(event: Event) {
        viewAdapter.updateDataSourceByEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateRequestReceived(updateEventRequest: UpdateEventRequest) {
        val view = binding.eventsRecyclerView.findViewHolderForAdapterPosition(updateEventRequest.adapterPosition) as EventViewHolder
        if (!view.event.voted) {
            MainActivity.instance.getEvent(updateEventRequest.eventId)
            view.showEventVoteAnimation()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onErrorReceived(errorCode: ErrorCodes) {
        if (errorCode == ErrorCodes.NO_EVENTS_FOUND) {
            viewAdapter.removeLoaderFromList()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationReceived(navigationCode: NavigationCode) {
        if (navigationCode == NavigationCode.LOAD_MORE_HAS_ENDED) {
            viewModel.isMoreLoading.value = false
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finishAffinity()
            exitProcess(0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.events_fragment, container, false)
        viewModel = ViewModelProvider(this).get(EventsViewModel::class.java)

        binding.addActionButton.setOnClickListener{ run {
            val action = EventsFragmentDirections.actionEventsFragmentToCreateEventFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }}

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (!viewModel.isMoreLoading.value!!)
                viewModel.refreshEvents()
            else
                binding.swipeRefreshLayout.isRefreshing = false
        }

        viewAdapter = EventEntryAdapter(AdapterClickListener { position ->
                val view = binding.eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventViewHolder
                val detailsDialogFragment = DetailsDialogFragment.newInstance(view.event)
                detailsDialogFragment.show(childFragmentManager, "detailsDialogFragment")
            },
            AdapterClickListener { position ->
                val view = binding.eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventViewHolder
                MainActivity.instance.modifyParticipation(view.event.id, view.event.accepted)
                view.showEventJoinAnimation()
            },
            AdapterClickListener { position ->
                val view = binding.eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventViewHolder
                val datesDialogFragment = DatesDialogFragment.newInstance(view.event.id, position)
                datesDialogFragment.show(childFragmentManager, "datesDialogFragment")
                MainActivity.instance.showDates(view.event.id)
        })
        viewAdapter.setHasStableIds(true)

        viewModel.events.observe(this, Observer { events ->
            events?.let { viewAdapter.addHeaderAndSubmitList(it) }
        })

        val layoutManager = LinearLayoutManager(context)

        binding.eventsRecyclerView.adapter = viewAdapter
        binding.eventsRecyclerView.layoutManager = layoutManager
        binding.eventsRecyclerView.setHasFixedSize(true)
        binding.eventsRecyclerView.setItemViewCacheSize(20)

        binding.eventsRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
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

        return binding.root
    }
}