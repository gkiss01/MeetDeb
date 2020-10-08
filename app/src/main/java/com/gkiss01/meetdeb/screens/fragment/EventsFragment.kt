package com.gkiss01.meetdeb.screens.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.EventViewHolder
import com.gkiss01.meetdeb.data.SuccessResponse
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.data.isAdmin
import com.gkiss01.meetdeb.network.Resource
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.utils.FastScrollerAdapter
import com.gkiss01.meetdeb.utils.getNavigationResult
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.viewmodels.EventsViewModel
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.addClickListener
import com.mikepenz.fastadapter.scroll.EndlessRecyclerOnScrollListener
import com.mikepenz.fastadapter.ui.items.ProgressItem
import com.mikepenz.itemanimators.AlphaInAnimator
import kotlinx.android.synthetic.main.fragment_events.*
import kotlinx.android.synthetic.main.item_event.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.core.parameter.parametersOf

typealias SuccessObserver = Observer<Resource<SuccessResponse<Long>>>
typealias EventsObserver = Observer<Resource<List<Event>>>

class EventsFragment : Fragment(R.layout.fragment_events) {
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private val viewModelKoin: EventsViewModel by sharedViewModel { parametersOf(viewModelActivityKoin.getBasic()) }

    private val itemAdapter = ItemAdapter<Event>()
    private val footerAdapter = ItemAdapter<ProgressItem>()
    private val fastScrollerAdapter = FastScrollerAdapter.with(listOf(itemAdapter, footerAdapter))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModelKoin.updateBasic(viewModelActivityKoin.getBasic())
        setHasOptionsMenu(true)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.action_event_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val eventsObserver = EventsObserver {
            when (it.status) {
                Status.SUCCESS -> {
                    ef_swipeRefreshLayout.isRefreshing = false
                    footerAdapter.clear()
                    viewModelKoin.addEventsToList(it.data!!)
                }
                Status.ERROR -> {
                    Toast.makeText(requireContext(), it.errorMessage, Toast.LENGTH_LONG).show()
                    ef_swipeRefreshLayout.isRefreshing = false
                    footerAdapter.clear()
                }
                Status.LOADING -> {
                    Log.d("MeetDebLog_EventsFragment", "Events are loading...")
                    footerAdapter.clear()
                    footerAdapter.add(ProgressItem())
                }
                else -> {}
            }
        }

        val deleteObserver = SuccessObserver {
            when (it.status) {
                Status.SUCCESS -> it.data?.withId?.let { eventId -> viewModelKoin.removeEventFromList(eventId) }
                Status.ERROR -> Toast.makeText(requireContext(), it.errorMessage, Toast.LENGTH_LONG).show()
                Status.LOADING -> Log.d("MeetDebLog_EventsFragment", "Deleting event...")
                else -> {}
            }
        }

        viewModelKoin.toastEvent.observeEvent(viewLifecycleOwner) {
            when (it) {
                is Int -> Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                is String -> Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        viewModelKoin.updateItemEvent.observeEvent(viewLifecycleOwner) {
            fastScrollerAdapter.notifyAdapterItemChanged(itemAdapter.getAdapterPosition(it))
        }

        ef_swipeRefreshLayout.setOnRefreshListener {
            if (!viewModelKoin.eventsIsLoading) viewModelKoin.refreshEvents().observe(viewLifecycleOwner, eventsObserver)
            else ef_swipeRefreshLayout.isRefreshing = false
        }

        if (viewModelKoin.events.value?.isEmpty() != false)
            viewModelKoin.refreshEvents().observe(viewLifecycleOwner, eventsObserver)

        fastScrollerAdapter.attachDefaultListeners = false

        ef_eventsRecyclerView.apply {
            adapter = fastScrollerAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = AlphaInAnimator()

            setHasFixedSize(true)
            setItemViewCacheSize(6)

            addOnScrollListener(object : EndlessRecyclerOnScrollListener(footerAdapter) {
                override fun onLoadMore(currentPage: Int) {
                    viewModelKoin.getMoreEvents().observe(viewLifecycleOwner, eventsObserver)
                }
            })
        }

        viewModelKoin.event.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.let { event -> viewModelKoin.updateEventInList(event) }
                    viewModelKoin.resetLiveData()
                }
                Status.ERROR -> {
                    Toast.makeText(requireContext(), it.errorMessage, Toast.LENGTH_LONG).show()
                    if (viewModelKoin.selectedEvent != Long.MIN_VALUE)
                        fastScrollerAdapter.notifyAdapterItemChanged(itemAdapter.getAdapterPosition(viewModelKoin.selectedEvent))
                    viewModelKoin.resetLiveData()
                    viewModelKoin.eventsIsLoading = false
                }
                Status.LOADING -> Log.d("MeetDebLog_EventsFragment", "Updating event...")
                else -> {}
            }
        })

        viewModelKoin.events.observe(viewLifecycleOwner, {
            FastAdapterDiffUtil[itemAdapter] = it
            viewModelKoin.eventsIsLoading = false
        })

        itemAdapter.fastAdapter!!.addClickListener( {null}, { vh: EventViewHolder -> listOf<View>(vh.itemView.eli_descButton, vh.itemView.eli_acceptButton, vh.itemView.eli_anotherDateButton, vh.itemView.eli_moreButton) }) { v, position, _, item ->
            when (v.id) {
                R.id.eli_descButton -> findNavController().navigate(EventsFragmentDirections.actionEventsFragmentToDetailsBottomSheetFragment(item))
                R.id.eli_acceptButton -> {
                    val itemView = ef_eventsRecyclerView.findViewHolderForAdapterPosition(position) as? EventViewHolder
                    itemView?.showJoinAnimation()

                    viewModelKoin.selectedEvent = item.id
                    viewModelKoin.modifyParticipation(item.id)
                }
                R.id.eli_anotherDateButton -> findNavController().navigate(EventsFragmentDirections.actionEventsFragmentToDatesDialogFragment(item))
                R.id.eli_moreButton -> createMoreActionMenu(v, item, deleteObserver)
            }
        }

        //Frissítjük az eventId eseményt, ha a DatesDialogFragment jelzi
        getNavigationResult<Long>(R.id.eventsFragment, "eventId") {
            val itemView = ef_eventsRecyclerView.findViewHolderForAdapterPosition(itemAdapter.getAdapterPosition(it)) as? EventViewHolder
            itemView?.showVoteAnimation()

            viewModelKoin.selectedEvent = it
            viewModelKoin.updateEvent(it)
        }
    }

    private fun createMoreActionMenu(view: View, event: Event, deleteObserver: SuccessObserver) {
        viewModelActivityKoin.activeUser.value?.data?.let {
            PopupMenu(context, view).apply {
                if (it.isAdmin()) {
                    if (event.userId == it.id) menu.add(0, R.id.update, 0, R.string.event_more_update)
                    if (event.reported) menu.add(0, R.id.removeReport, 0, R.string.event_more_remove_report)
                    menu.add(0, R.id.delete, 0, R.string.event_more_delete)
                } else {
                    if (event.userId == it.id) {
                        menu.add(0, R.id.update, 0, R.string.event_more_update)
                        menu.add(0, R.id.delete, 0, R.string.event_more_delete)
                    }
                    else menu.add(0, R.id.report, 0, R.string.event_more_report)
                }

                setOnMenuItemClickListener { menu ->
                    when (menu.itemId) {
                        R.id.report -> viewModelKoin.createReport(event.id)
                        R.id.removeReport -> viewModelKoin.deleteReport(event.id)
                        R.id.delete -> viewModelKoin.deleteEvent(event.id).observe(viewLifecycleOwner, deleteObserver)
                        R.id.update -> findNavController().navigate(EventsFragmentDirections.actionEventsFragmentToEventCreateFragment(event))
                    }
                    true
                }
                show()
            }
        }
    }
}