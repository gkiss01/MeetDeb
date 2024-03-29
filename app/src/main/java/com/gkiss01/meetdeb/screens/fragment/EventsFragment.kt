package com.gkiss01.meetdeb.screens.fragment

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.remote.response.Event
import com.gkiss01.meetdeb.data.remote.response.isAdmin
import com.gkiss01.meetdeb.databinding.FragmentEventsBinding
import com.gkiss01.meetdeb.screens.viewholders.EventViewHolder
import com.gkiss01.meetdeb.utils.FastScrollerAdapter
import com.gkiss01.meetdeb.utils.addOnScrollListener
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ViewModelOwner.Companion.from
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class EventsFragment : Fragment(R.layout.fragment_events) {
    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private val viewModelKoin: EventsViewModel by viewModel(owner = {
        from(findNavController().getViewModelStoreOwner(R.id.navigation_graph_main))
    })

    private val itemAdapter = ItemAdapter<Event>()
    private val footerAdapter = ItemAdapter<ProgressItem>()
    private val fastScrollerAdapter = FastScrollerAdapter.with(listOf(itemAdapter, footerAdapter)).apply {
        attachDefaultListeners = false
    }

    private val endlessScrollListener = object : EndlessRecyclerOnScrollListener(footerAdapter) {
        override fun onLoadMore(currentPage: Int) {
            viewModelKoin.loadEventsForPage(if (currentPage == 0) 1 else (viewModelKoin.lastPage + 1))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_event_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEventsBinding.bind(view)

        binding.recyclerView.apply {
            adapter = fastScrollerAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = null

            setHasFixedSize(true)
            setItemViewCacheSize(6)

            addOnScrollListener(viewLifecycleOwner, endlessScrollListener)
        }

        // Toast üzenet
        viewModelKoin.toastEvent.observeEvent(viewLifecycleOwner) {
            when (it) {
                is Int -> Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                is String -> Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        // Esemény gomb animációk kezelése
        viewModelKoin.itemCurrentlyUpdating.observe(viewLifecycleOwner) {
            it?.let { item ->
                fastScrollerAdapter.notifyAdapterItemChanged(itemAdapter.getAdapterPosition(item.second))
            }
        }

        // Adott lista elem befrissítése
        viewModelKoin.updateItemEvent.observeEvent(viewLifecycleOwner) {
            fastScrollerAdapter.notifyAdapterItemChanged(itemAdapter.getAdapterPosition(it))
        }

        // ViewHolder frissítési csomagok összeállítása
        fastScrollerAdapter.onBindViewHolderListener = object : OnBindViewHolderListenerImpl<GenericItem>() {
            override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
                val additionalPayload = listOfNotNull(viewModelKoin.itemCurrentlyUpdating.value)
                super.onBindViewHolder(viewHolder, position, payloads + additionalPayload)
            }
        }

        // Esemény lista újratöltése
        viewModelKoin.events.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    FastAdapterDiffUtil.calculateDiff(itemAdapter, it)
                }
                FastAdapterDiffUtil[itemAdapter] = result
            }
        }

        // Footer animáció kezelése
        viewModelKoin.footerCurrentlyNeeded.observe(viewLifecycleOwner) {
            footerAdapter.clear()
            if (it) footerAdapter.add(ProgressItem())
            else binding.swipeRefreshLayout.isRefreshing = false
        }

        itemAdapter.fastAdapter?.addClickListener( {null}, { vh: EventViewHolder -> listOf(vh.binding.descButton,
            vh.binding.acceptButton, vh.binding.anotherDateButton, vh.binding.moreButton) }) { v, _, _, item ->
            when (v.id) {
                R.id.descButton -> findNavController().navigate(EventsFragmentDirections.actionEventsFragmentToDetailsBottomSheetFragment(item))
                R.id.acceptButton -> viewModelKoin.modifyParticipation(item.id)
                R.id.anotherDateButton -> findNavController().navigate(EventsFragmentDirections.actionEventsFragmentToDatesDialogFragment(item))
                R.id.moreButton -> createMoreActionMenu(v, item)
            }
        }

        getNavigationResult<Long>(R.id.eventsFragment, "eventId") {
            viewModelKoin.updateEvent(it)
        }

        // PullToRefresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (viewModelKoin.footerCurrentlyNeeded.value == true) binding.swipeRefreshLayout.isRefreshing = false
            else endlessScrollListener.resetPageCount(0)
        }

        if (viewModelKoin.events.value?.isEmpty() != false)
            endlessScrollListener.resetPageCount(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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