package com.gkiss01.meetdeb.viewmodels

import androidx.lifecycle.*
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.network.Resource
import com.gkiss01.meetdeb.network.RestClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.dsl.module

val eventsModule = module {
    factory { (basic: String) -> EventsViewModel(get(), basic) }
}

class EventsViewModel(private val restClient: RestClient, private var basic: String) : ViewModel() {
    var selectedEvent = Long.MIN_VALUE
    var eventsIsLoading = false
    private var currentPage: Int = 1

    private var _event = MutableLiveData<Resource<Event>>()
    val event: LiveData<Resource<Event>>
        get() = _event

    private var _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>>
        get() = _events

    fun updateBasic(basic: String) {
        this.basic = basic
    }

    fun refreshEvents(): LiveData<Resource<List<Event>>> {
        currentPage = 1
        eventsIsLoading = true
        return getEvents(currentPage)
    }

    fun getMoreEvents(): LiveData<Resource<List<Event>>> {
        currentPage = ((_events.value?.size ?: 0) / 25) + 1
        eventsIsLoading = true
        return getEvents(currentPage)
    }

    private fun getEvents(page: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(null))
        emit(restClient.getEventsAsync(basic, page))
    }

    fun updateEvent(eventId: Long) {
        _event.postValue(Resource.loading(null))
        viewModelScope.launch {
            _event.postValue(restClient.getEventAsync(basic, eventId))
        }
    }

    fun deleteEvent(eventId: Long) = liveData(Dispatchers.IO) {
        emit(Resource.loading(null))
        emit(restClient.deleteEventAsync(basic, eventId))
    }

    fun modifyParticipation(eventId: Long) {
        _event.postValue(Resource.loading(null))
        viewModelScope.launch {
            _event.postValue(restClient.modifyParticipation(basic, eventId))
        }
    }

    fun createReport(eventId: Long) = liveData(Dispatchers.IO) {
        emit(Resource.loading(null))
        emit(restClient.createReportAsync(basic, eventId))
    }

    fun deleteReport(eventId: Long) = liveData(Dispatchers.IO) {
        emit(Resource.loading(null))
        emit(restClient.deleteReportAsync(basic, eventId))
    }

    fun resetLiveData() {
        selectedEvent = Long.MIN_VALUE
        _event.postValue(Resource.pending(null))
    }

    fun addEventsToList(events: List<Event>) {
        if (currentPage > 1) _events.value = _events.value?.union(events)?.toList()
        else _events.value = events
    }

    fun updateEventInList(event: Event) {
        _events.postValue(_events.value?.map { if (it.id == event.id) event else it })
    }

    fun removeEventFromList(eventId: Long) {
        _events.postValue(_events.value?.filterNot { it.id == eventId })
    }

    fun addEventReportToList(eventId: Long) {
        _events.value?.find { it.id == eventId }?.reported = true
    }

    fun removeEventReportFromList(eventId: Long) {
        _events.value?.find { it.id == eventId }?.reported = false
    }
}
