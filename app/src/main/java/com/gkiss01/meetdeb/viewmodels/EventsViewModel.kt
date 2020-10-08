package com.gkiss01.meetdeb.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.network.PAGE_SIZE
import com.gkiss01.meetdeb.network.Resource
import com.gkiss01.meetdeb.network.RestClient
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.utils.SingleEvent
import kotlinx.coroutines.launch
import org.koin.dsl.module

val eventsModule = module {
    factory { (basic: String) -> EventsViewModel(get(), basic) }
}

class EventsViewModel(private val restClient: RestClient, private var basic: String) : ViewModel() {
    var selectedEvent = Long.MIN_VALUE
    var eventsIsLoading = false
    private var currentPage: Int = 1

    private val _toastEvent = MutableLiveData<SingleEvent<Any>>()
    val toastEvent: LiveData<SingleEvent<Any>>
        get() = _toastEvent

    private val _updateItemEvent = MutableLiveData<SingleEvent<Long>>()
    val updateItemEvent: LiveData<SingleEvent<Long>>
        get() = _updateItemEvent

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
        currentPage = ((_events.value?.size ?: 0) / PAGE_SIZE) + 1
        eventsIsLoading = true
        return getEvents(currentPage)
    }

    private fun getEvents(page: Int) = liveData {
        emit(Resource.loading(null))
        emit(restClient.getEvents(basic, page))
    }

    fun updateEvent(eventId: Long) {
        _event.postValue(Resource.loading(null))
        viewModelScope.launch {
            _event.postValue(restClient.getEvent(basic, eventId))
        }
    }

    fun deleteEvent(eventId: Long) = liveData {
        emit(Resource.loading(null))
        emit(restClient.deleteEvent(basic, eventId))
    }

    fun modifyParticipation(eventId: Long) {
        _event.postValue(Resource.loading(null))
        viewModelScope.launch {
            _event.postValue(restClient.modifyParticipation(basic, eventId))
        }
    }

    fun createReport(eventId: Long) {
        Log.d("MeetDebLog_EventsViewModel", "Creating event report with event ID $eventId ...")
        viewModelScope.launch {
            restClient.createReport(basic, eventId).let {
                when (it.status) {
                    Status.SUCCESS -> it.data?.withId?.let { eventId ->
                        addReportToEvent(eventId)
                        _updateItemEvent.postValue(SingleEvent(eventId))
                        _toastEvent.postValue(SingleEvent(R.string.event_reported))
                    }
                    Status.ERROR -> _toastEvent.postValue(SingleEvent(it.errorMessage))
                    else -> {}
                }
            }
        }
    }

    fun deleteReport(eventId: Long) {
        Log.d("MeetDebLog_EventsViewModel", "Deleting event report with event ID $eventId ...")
        viewModelScope.launch {
            restClient.deleteReport(basic, eventId).let {
                when (it.status) {
                    Status.SUCCESS -> it.data?.withId?.let { eventId ->
                        removeReportFromEvent(eventId)
                        _updateItemEvent.postValue(SingleEvent(eventId))
                        _toastEvent.postValue(SingleEvent(R.string.event_report_removed))
                    }
                    Status.ERROR -> _toastEvent.postValue(SingleEvent(it.errorMessage))
                    else -> {}
                }
            }
        }
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

    private fun addReportToEvent(eventId: Long) {
        _events.value?.find { it.id == eventId }?.reported = true
    }

    private fun removeReportFromEvent(eventId: Long) {
        _events.value?.find { it.id == eventId }?.reported = false
    }
}
