package com.gkiss01.meetdeb.screens

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager

import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.EventClickListener
import com.gkiss01.meetdeb.adapter.EventEntryAdapter
import com.gkiss01.meetdeb.data.EventDatabase
import com.gkiss01.meetdeb.databinding.EventsFragmentBinding

class EventsFragment : Fragment() {

    private lateinit var binding: EventsFragmentBinding
    private lateinit var viewModel: EventsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.events_fragment, container, false)

        val application = requireNotNull(this.activity).application
        val eventDataSource = EventDatabase.getInstance(application).eventEntryDao
        val participantDataSource = EventDatabase.getInstance(application).participantEntryDao
        val viewModelFactory = EventsViewModelFactory(eventDataSource, participantDataSource, application)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EventsViewModel::class.java)

        binding.addActionButton.setOnClickListener{ run {
            val action = EventsFragmentDirections.actionEventsFragmentToCreateEventFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }}

        val viewAdapter = EventEntryAdapter(EventClickListener { eventId ->
            viewModel.createParticipant(eventId)
        })

        viewModel.eventEntries.observe(this, Observer { events ->
            events?.let { viewAdapter.addHeaderAndSubmitList(it) }
        })

        binding.eventsRecyclerView.adapter = viewAdapter
        binding.eventsRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }
}
