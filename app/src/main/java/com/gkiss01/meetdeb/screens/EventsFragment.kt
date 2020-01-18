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
import com.bumptech.glide.ListPreloader.PreloadModelProvider
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.FixedPreloadSizeProvider
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.EventClickListener
import com.gkiss01.meetdeb.adapter.EventEntryAdapter
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.databinding.EventsFragmentBinding
import com.gkiss01.meetdeb.network.BASE_URL
import com.gkiss01.meetdeb.network.GlideApp
import com.gkiss01.meetdeb.network.GlideRequests
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
    fun onEventsReceived(events: List<Event>) {
        viewModel.events.value = events
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventReceived(event: Event) {
        viewAdapter.updateDataSourceByEvent(event)
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

        viewAdapter = EventEntryAdapter(glide,
            EventClickListener { position ->
                val view = binding.eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventEntryAdapter.EntryViewHolder
                view.showEventDetails()
            },
            EventClickListener { position ->
            val view = binding.eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventEntryAdapter.EntryViewHolder
            MainActivity.instance.modifyParticipation(view.eventId, view.eventAccepted)
            //view.showEventJoinAnimation()
            })

        viewModel.events.observe(this, Observer { events ->
            events?.let { viewAdapter.addHeaderAndSubmitList(it) }
        })

        val sizeProvider = FixedPreloadSizeProvider<String>(1080, 1080)
        val modelProvider = CustomPreloadModelProvider()
        val recyclerViewPreloader: RecyclerViewPreloader<String> = RecyclerViewPreloader(glide, modelProvider, sizeProvider, 10)

        binding.eventsRecyclerView.addOnScrollListener(recyclerViewPreloader)
        binding.eventsRecyclerView.adapter = viewAdapter
        binding.eventsRecyclerView.layoutManager = LinearLayoutManager(context)

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

