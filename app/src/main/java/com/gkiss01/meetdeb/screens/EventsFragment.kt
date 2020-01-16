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
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.EventClickListener
import com.gkiss01.meetdeb.adapter.EventEntryAdapter
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.databinding.EventsFragmentBinding
import com.gkiss01.meetdeb.network.BASE_URL
import com.gkiss01.meetdeb.network.GlideApp
import com.gkiss01.meetdeb.network.GlideRequests
import java.util.*

class EventsFragment : Fragment(), GenericResponseListener {

    private lateinit var binding: EventsFragmentBinding
    private lateinit var viewModel: EventsViewModel
    private lateinit var viewAdapter: EventEntryAdapter
    private lateinit var glide: GlideRequests

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.events_fragment, container, false)
        glide = GlideApp.with(activity!!)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = EventsViewModelFactory(application)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EventsViewModel::class.java)

        binding.addActionButton.setOnClickListener{ run {
            val action = EventsFragmentDirections.actionEventsFragmentToCreateEventFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }}

        viewModel.genericResponseListener.value = this
        viewAdapter = EventEntryAdapter(glide,
            EventClickListener { position ->
                val view = binding.eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventEntryAdapter.EntryViewHolder
                view.showEventDetails()
            },
            EventClickListener { position ->
            val view = binding.eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventEntryAdapter.EntryViewHolder
            viewModel.modifyParticipation(view.eventId, view.eventAccepted)
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

    override fun onEventReceive(event: Event) {
        viewAdapter.updateDataSourceByEvent(event)
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

