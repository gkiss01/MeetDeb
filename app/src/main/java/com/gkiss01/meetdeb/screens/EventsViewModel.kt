package com.gkiss01.meetdeb.screens

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.data.Event

class EventsViewModel : ViewModel() {
    val events = MutableLiveData<List<Event>>()
    var selectedEvent = Long.MIN_VALUE
    var isMoreLoading = true
    private var currentPage = 1

    init {
        MainActivity.instance.getEvents()
    }

    fun refreshEvents() {
        currentPage = 1
        MainActivity.instance.getEvents()
    }

    fun loadMoreEvents() {
        currentPage++
        MainActivity.instance.getEvents(currentPage)
        isMoreLoading = true
    }

    fun addEvents(eventList: List<Event>) {
        if (currentPage > 1) events.value = events.value?.union(eventList)?.toList()
        else events.value = eventList
    }

    fun updateEvent(event: Event) {
        events.value = events.value!!.map { if (it.id == event.id) event else it }
    }
}
