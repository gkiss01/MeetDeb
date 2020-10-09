package com.gkiss01.meetdeb.screens.fragment

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.EventViewHolder
import com.gkiss01.meetdeb.data.SuccessResponse
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.data.isAdmin
import com.gkiss01.meetdeb.network.Resource
import com.gkiss01.meetdeb.utils.FastScrollerAdapter
import com.gkiss01.meetdeb.utils.getNavigationResult
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.viewmodels.EventsViewModel
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.OnBindViewHolderListenerImpl
import com.mikepenz.fastadapter.listeners.addClickListener
import com.mikepenz.fastadapter.scroll.EndlessRecyclerOnScrollListener
import com.mikepenz.fastadapter.ui.items.ProgressItem
import com.mikepenz.itemanimators.AlphaInAnimator
import kotlinx.android.synthetic.main.fragment_events.*
import kotlinx.android.synthetic.main.item_event.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.core.parameter.parametersOf

typealias SuccessObserver = Observer<Resource<SuccessResponse<Long>>>

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
        fastScrollerAdapter.attachDefaultListeners = false
        val endlessScrollListener = object : EndlessRecyclerOnScrollListener(footerAdapter) {
            override fun onLoadMore(currentPage: Int) {
                viewModelKoin.loadEventsForPage(currentPage + 1)
            }
        }

        ef_eventsRecyclerView.apply {
            adapter = fastScrollerAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = AlphaInAnimator()

            setHasFixedSize(true)
            setItemViewCacheSize(6)
            addOnScrollListener(endlessScrollListener)
        }

        itemAdapter.fastAdapter?.addClickListener( {null}, { vh: EventViewHolder -> listOf<View>(vh.itemView.eli_descButton,
            vh.itemView.eli_acceptButton, vh.itemView.eli_anotherDateButton, vh.itemView.eli_moreButton) }) { v, _, _, item ->
            when (v.id) {
                R.id.eli_descButton -> findNavController().navigate(EventsFragmentDirections.actionEventsFragmentToDetailsBottomSheetFragment(item))
                R.id.eli_acceptButton -> viewModelKoin.modifyParticipation(item.id)
                R.id.eli_anotherDateButton -> findNavController().navigate(EventsFragmentDirections.actionEventsFragmentToDatesDialogFragment(item))
                R.id.eli_moreButton -> createMoreActionMenu(v, item)
            }
        }

        // Toast üzenet
        viewModelKoin.toastEvent.observeEvent(viewLifecycleOwner) {
            when (it) {
                is Int -> Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                is String -> Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        // Adott lista elem befrissítése
        viewModelKoin.updateItemEvent.observeEvent(viewLifecycleOwner) {
            fastScrollerAdapter.notifyAdapterItemChanged(itemAdapter.getAdapterPosition(it))
        }

        // Esemény gomb animációk kezelése
        viewModelKoin.itemCurrentlyUpdating.observe(viewLifecycleOwner) {
            it?.let { item ->
                getEventViewHolderByPosition(itemAdapter.getAdapterPosition(item.second))?.manageAnimation(item.first)
            }
        }

        fastScrollerAdapter.onBindViewHolderListener = object : OnBindViewHolderListenerImpl<GenericItem>() {
            override fun onViewAttachedToWindow(viewHolder: RecyclerView.ViewHolder, position: Int) {
                viewModelKoin.itemCurrentlyUpdating.value?.let { item ->
                    (viewHolder as? EventViewHolder)?.let {
                        if (it.event.id == item.second) it.manageAnimation(item.first)
                    }
                }
            }
        }

        getNavigationResult<Long>(R.id.eventsFragment, "eventId") {
            viewModelKoin.updateEvent(it)
        }

        // Footer animáció kezelése
        viewModelKoin.footerCurrentlyNeeded.observe(viewLifecycleOwner) {
            if (it) {
                footerAdapter.clear()
                footerAdapter.add(ProgressItem())
            } else {
                ef_swipeRefreshLayout.isRefreshing = false
                footerAdapter.clear()
            }
        }

        // Esemény lista újratöltése
        viewModelKoin.events.observe(viewLifecycleOwner, {
            FastAdapterDiffUtil[itemAdapter] = it
        })

        // PullToRefresh
        ef_swipeRefreshLayout.setOnRefreshListener {
            if (viewModelKoin.footerCurrentlyNeeded.value == true) ef_swipeRefreshLayout.isRefreshing = false
            else endlessScrollListener.resetPageCount(0)
        }

        if (viewModelKoin.events.value?.isEmpty() != false)
            endlessScrollListener.resetPageCount(0)
    }

    private fun getEventViewHolderByPosition(position: Int) = ef_eventsRecyclerView.findViewHolderForAdapterPosition(position) as? EventViewHolder

    private fun createMoreActionMenu(view: View, event: Event) {
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
                        R.id.delete -> viewModelKoin.deleteEvent(event.id)
                        R.id.update -> findNavController().navigate(EventsFragmentDirections.actionEventsFragmentToEventCreateFragment(event))
                    }
                    true
                }
                show()
            }
        }
    }
}