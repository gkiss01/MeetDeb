package com.gkiss01.meetdeb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.network.Resource
import com.gkiss01.meetdeb.network.RestClient
import kotlinx.coroutines.launch
import org.koin.dsl.module

val eventsModule = module {
    factory { (basic: String) -> EventsViewModel(get(), basic) }
}

class EventsViewModel(private val restClient: RestClient, private val basic: String) : ViewModel() {
    var selectedEvent = Long.MIN_VALUE
    private var currentPage: Int = 1

    private var _events = MutableLiveData<Resource<List<Event>>>()
    val events: LiveData<Resource<List<Event>>>
        get() = _events

    fun refreshEvents() {
        currentPage = 1
        getEvents(currentPage)
    }

    fun getMoreEvents() {
        currentPage++
        getEvents(currentPage)
    }

    private fun getEvents(page: Int) {
        _events.postValue(Resource.loading(null))
        viewModelScope.launch {
            _events.postValue(restClient.getEventsAsync(basic, page))
        }
    }

    fun updateEvent(eventId: Long) {
        Log.d("MeetDebLog_EventsFragment", "Updating event...")
    }

    init {
        refreshEvents()
    }

//    fun addEvents(eventList: List<Event>) {
//        if (currentPage > 1) events.value = events.value?.union(eventList)?.toList()
//        else events.value = eventList
//    }
//
//    fun updateEvent(event: Event) {
//        events.value = events.value!!.map { if (it.id == event.id) event else it }
//    }
//
//    fun deleteEvent(eventId: Long) {
//        events.value = events.value!!.filterNot { it.id == eventId }
//    }
}
