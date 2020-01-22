package com.gkiss01.meetdeb.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.ListPreloader.PreloadModelProvider
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.FixedPreloadSizeProvider
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.AdapterClickListener
import com.gkiss01.meetdeb.adapter.EventEntryAdapter
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.data.EventList
import com.gkiss01.meetdeb.databinding.EventsFragmentBinding
import com.gkiss01.meetdeb.network.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


class EventsFragment : Fragment() {

    private lateinit var binding: EventsFragmentBinding
    private lateinit var viewModel: EventsViewModel
    private lateinit var viewAdapter: EventEntryAdapter
    private lateinit var glide: GlideRequests

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
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
    fun onErrorReceived(errorCode: ErrorCode) {
        when (errorCode) {
            ErrorCode.ERROR_NO_EVENTS_FOUND -> {
                viewAdapter.removeLoaderFromList()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationReceived(navigationCode: NavigationCode) {
        if (navigationCode == NavigationCode.LOAD_MORE_HAS_ENDED) {
            viewModel.isMoreLoading.value = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.events_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(EventsViewModel::class.java)
        glide = GlideApp.with(activity!!)

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

        viewAdapter = EventEntryAdapter(glide,
            AdapterClickListener { position ->
                val view = binding.eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventEntryAdapter.EventViewHolder
                view.showEventDetails()
            },
            AdapterClickListener { position ->
                val view = binding.eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventEntryAdapter.EventViewHolder
                MainActivity.instance.modifyParticipation(view.eventId, view.eventAccepted)
                view.showEventJoinAnimation()
            },
            AdapterClickListener { position ->
                val view = binding.eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventEntryAdapter.EventViewHolder
                val datesDialogFragment = DatesDialogFragment()
                datesDialogFragment.show(requireFragmentManager(), "datesDialogFragment")
                MainActivity.instance.showDates(view.eventId)
        })

        viewModel.events.observe(this, Observer { events ->
            events?.let { viewAdapter.addHeaderAndSubmitList(it) }
        })

        val sizeProvider = FixedPreloadSizeProvider<String>(1080, 1080)
        val modelProvider = CustomPreloadModelProvider()
        val recyclerViewPreloader: RecyclerViewPreloader<String> = RecyclerViewPreloader(glide, modelProvider, sizeProvider, 10)

        val layoutManager = LinearLayoutManager(context)

        binding.eventsRecyclerView.adapter = viewAdapter
        binding.eventsRecyclerView.layoutManager = layoutManager

        binding.eventsRecyclerView.addOnScrollListener(recyclerViewPreloader)
        binding.eventsRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0 && !viewModel.isMoreLoading.value!!) {
//                    if (layoutManager.findLastVisibleItemPosition() == viewModel.events.value!!.size - 1)
                    if (layoutManager.itemCount <= (layoutManager.findLastVisibleItemPosition() + 1)) {
                        viewModel.loadMoreEvents()
                        viewAdapter.addLoaderToList()
                    }
                }
            }
        })

        return binding.root
    }

    private inner class CustomPreloadModelProvider: PreloadModelProvider<String> {
        override fun getPreloadItems(position: Int): MutableList<String> {
            if (position >= viewModel.events.value!!.size) return Collections.emptyList()
            return Collections.singletonList("${BASE_URL}/images/${viewModel.events.value!![position].id}")
        }

        override fun getPreloadRequestBuilder(item: String): RequestBuilder<*> {
            return glide.load(item)
        }
    }
}