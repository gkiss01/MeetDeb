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
    private var currentPage: Int = 1
    private lateinit var eventsBackup: List<Event>

    private var _event = MutableLiveData<Resource<Event>>()
    val event: LiveData<Resource<Event>>
        get() = _event

    private var _events = MutableLiveData<Resource<List<Event>>>()
    val events: LiveData<Resource<List<Event>>>
        get() = _events

    fun updateBasic(basic: String) {
        this.basic = basic
    }

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
        backupEvents()
        viewModelScope.launch {
            _events.postValue(restClient.getEventsAsync(basic, page))
        }
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

    private fun backupEvents() {
        events.value?.data?.let {
            eventsBackup = it
        }
    }

    fun restoreEventsIfNeeded() {
        if (::eventsBackup.isInitialized) _events.postValue(Resource.success(eventsBackup))
        else _events.postValue(Resource.success(emptyList()))
    }

    init {
        refreshEvents()
    }

//    fun addEvents(eventList: List<Event>) {
//        if (currentPage > 1) events.value = events.value?.union(eventList)?.toList()
//        else events.value = eventList
//    }
//

    fun updateEventInList(event: Event) {
        _events.postValue(Resource.success(_events.value?.data?.map { if (it.id == event.id) event else it }))
    }

    fun removeEventFromList(eventId: Long) {
        _events.postValue(Resource.success(_events.value?.data?.filterNot { it.id == eventId }))
    }

    fun addEventReportToList(eventId: Long) {
        _events.value?.data?.find { it.id == eventId }?.reported = true
    }

    fun removeEventReportFromList(eventId: Long) {
        _events.value?.data?.find { it.id == eventId }?.reported = false
    }
}
