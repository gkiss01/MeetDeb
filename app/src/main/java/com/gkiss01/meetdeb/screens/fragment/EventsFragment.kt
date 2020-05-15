package com.gkiss01.meetdeb.screens.fragment

import ScrollingViewOnApplyWindowInsetsListener
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.EventViewHolder
import com.gkiss01.meetdeb.data.EventList
import com.gkiss01.meetdeb.data.adapterrequest.DeleteEventRequest
import com.gkiss01.meetdeb.data.adapterrequest.UpdateEventRequest
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.data.isAdmin
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.viewmodels.EventsViewModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericFastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.addClickListener
import com.mikepenz.fastadapter.ui.items.ProgressItem
import com.mikepenz.itemanimators.AlphaInAnimator
import kotlinx.android.synthetic.main.fragment_events.*
import kotlinx.android.synthetic.main.item_event.view.*
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EventsFragment : Fragment(R.layout.fragment_events) {
    private val viewModel: EventsViewModel by activityViewModels()
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()

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

        view.showVoteAnimation()
        viewModel.selectedEvent = request.eventId

        MainActivity.instance.getEvent(request.eventId)
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
        val accountHeaderView = createSliderHeader(ef_slider)
        addSliderItems(ef_slider, 1)
        addSliderNavigation()

        viewModelActivityKoin.activeUser.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    accountHeaderView.currentProfileName.text = it.data?.name
                    accountHeaderView.currentProfileEmail.text = it.data?.email
                }
                Status.PENDING -> findNavController().navigate(R.id.registerFragment)
                else -> Log.e("MeetDebLog_EventsFragment", "User is null...")
            }
        })

        ef_addActionButton.setOnClickListener{ findNavController().navigate(R.id.createEventFragment) }

        ef_swipeRefreshLayout.setOnRefreshListener {
            if (!viewModel.isMoreLoading)
                viewModel.refreshEvents()
            else ef_swipeRefreshLayout.isRefreshing = false
        }

        fastAdapter = FastAdapter.with(listOf(itemAdapter, footerAdapter))
        fastAdapter.attachDefaultListeners = false
        ef_eventsRecyclerView.adapter = fastAdapter

        val fastScroller = FastScrollerBuilder(ef_eventsRecyclerView).useMd2Style().build()
        ef_eventsRecyclerView.setOnApplyWindowInsetsListener(ScrollingViewOnApplyWindowInsetsListener(ef_eventsRecyclerView, fastScroller))

        val layoutManager = LinearLayoutManager(requireContext())
        ef_eventsRecyclerView.layoutManager = layoutManager

        ef_eventsRecyclerView.setHasFixedSize(true)
        ef_eventsRecyclerView.setItemViewCacheSize(6)
        ef_eventsRecyclerView.itemAnimator = AlphaInAnimator()

        viewModel.events.observe(viewLifecycleOwner, Observer {
            FastAdapterDiffUtil[itemAdapter] = it
            ef_swipeRefreshLayout.isRefreshing = false
            viewModel.isMoreLoading = false // félős
            footerAdapter.clear()
        })

        itemAdapter.fastAdapter!!.addClickListener( {null}, { vh: EventViewHolder -> listOf<View>(vh.itemView.eli_descButton, vh.itemView.eli_acceptButton, vh.itemView.eli_anotherDateButton, vh.itemView.eli_moreButton) }) { v, position, _, item ->
            when (v.id) {
                R.id.eli_descButton -> findNavController().navigate(EventsFragmentDirections.actionEventsFragmentToDetailsBottomSheetFragment(item))
                R.id.eli_acceptButton -> {
                    MainActivity.instance.modifyParticipation(item.id, item.accepted)

                    val itemView = ef_eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventViewHolder
                    itemView.showJoinAnimation()
                }
                R.id.eli_anotherDateButton -> {
                    MainActivity.instance.showDates(item.id)

                    findNavController().navigate(EventsFragmentDirections.actionEventsFragmentToDatesDialogFragment(item))
                }
                R.id.eli_moreButton -> {
                    createMoreActionMenu(v, item)
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

    private fun addSliderNavigation() {
        ef_slider.onDrawerItemClickListener = { _, item, _ ->
            when (item.identifier) {
                2L -> findNavController().navigate(R.id.profileFragment)
                3L -> viewModelActivityKoin.resetLiveData()
            }
            false
        }
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

                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.report ->
                            MainActivity.instance.reportEvent(event.id)
                        R.id.removeReport ->
                            MainActivity.instance.removeReport(event.id)
                        R.id.delete ->
                            MainActivity.instance.deleteEvent(event.id)
                        R.id.update -> findNavController().navigate(EventsFragmentDirections.actionEventsFragmentToCreateEventFragment(event))
                    }
                    true
                }
                show()
            }
        }
    }
}