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
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.EventClickListener
import com.gkiss01.meetdeb.adapter.EventEntryAdapter
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.databinding.EventsFragmentBinding

class EventsFragment : Fragment(), GenericResponseListener {

    private lateinit var binding: EventsFragmentBinding
    private lateinit var viewModel: EventsViewModel
    private lateinit var viewAdapter: EventEntryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.events_fragment, container, false)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = EventsViewModelFactory(application)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EventsViewModel::class.java)

        binding.addActionButton.setOnClickListener{ run {
            val action = EventsFragmentDirections.actionEventsFragmentToCreateEventFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }}

        viewModel.genericResponseListener.value = this
        viewAdapter = EventEntryAdapter(EventClickListener { position ->
            val view = binding.eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventEntryAdapter.EntryViewHolder
            view.showEventDetails()
        },EventClickListener { position ->
            val view = binding.eventsRecyclerView.findViewHolderForAdapterPosition(position) as EventEntryAdapter.EntryViewHolder
            viewModel.modifyParticipation(view.eventId, view.eventAccepted)
            //view.showEventJoinAnimation()
        })

        viewModel.events.observe(this, Observer { events ->
            events?.let { viewAdapter.addHeaderAndSubmitList(it) }
        })

        binding.eventsRecyclerView.adapter = viewAdapter
        binding.eventsRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onEventReceive(event: Event) {
        viewAdapter.updateDataSourceByEvent(event)
    }
}
