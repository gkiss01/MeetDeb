package com.gkiss01.meetdeb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.network.RestClient
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.utils.SingleEvent
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val eventsModule = module {
    factory { EventsViewModel(get()) }
    factory { EventCreateViewModel(get(), get(), androidApplication()) }
    factory { DatesViewModel(get()) }
    factory { ParticipantsViewModel(get()) }
}

typealias ItemUpdating = Pair<Event.UpdatingType, Long>

class EventsViewModel(private val restClient: RestClient) : ViewModel() {
    private val _toastEvent = MutableLiveData<SingleEvent<Any>>()
    val toastEvent: LiveData<SingleEvent<Any>>
        get() = _toastEvent

    private val _updateItemEvent = MutableLiveData<SingleEvent<Long>>()
    val updateItemEvent: LiveData<SingleEvent<Long>>
        get() = _updateItemEvent

    private val _itemCurrentlyUpdating = MutableLiveData<ItemUpdating?>()
    val itemCurrentlyUpdating: LiveData<ItemUpdating?>
        get() = _itemCurrentlyUpdating

    private val _footerCurrentlyNeeded = MutableLiveData<Boolean>()
    val footerCurrentlyNeeded: LiveData<Boolean>
        get() = _footerCurrentlyNeeded

    private var _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>>
        get() = _events

    var lastPage: Int = 0
        private set

    fun loadEventsForPage(page: Int) {
        if (_footerCurrentlyNeeded.value == true) return
        Log.d("MeetDebLog_EventsViewModel", "Events are loading...")
        _footerCurrentlyNeeded.postValue(true)
        viewModelScope.launch {
            restClient.getEvents(page).let {
                _footerCurrentlyNeeded.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { events -> addEventsToList(events, page) }
                    Status.ERROR -> _toastEvent.postValue(SingleEvent(it.errorMessage))
                    else -> {}
                }
            }
        }
    }

    fun updateEvent(eventId: Long) {
        Log.d("MeetDebLog_EventsViewModel", "Updating event with ID $eventId ...")
        _itemCurrentlyUpdating.postValue(Pair(Event.UpdatingType.VOTE, eventId))
        viewModelScope.launch {
            restClient.getEvent(eventId).let {
                _itemCurrentlyUpdating.postValue(null)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { event -> updateEventInList(event) }
                    Status.ERROR -> {
                        _updateItemEvent.postValue(SingleEvent(eventId))
                        _toastEvent.postValue(SingleEvent(it.errorMessage))
                    }
                    else -> {}
                }
            }
        }
    }

    fun modifyParticipation(eventId: Long) {
        Log.d("MeetDebLog_EventsViewModel", "Modifying participation with event ID $eventId ...")
        _itemCurrentlyUpdating.postValue(Pair(Event.UpdatingType.PARTICIPATION, eventId))
        viewModelScope.launch {
            restClient.modifyParticipation(eventId).let {
                _itemCurrentlyUpdating.postValue(null)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { event -> updateEventInList(event) }
                    Status.ERROR -> {
                        _updateItemEvent.postValue(SingleEvent(eventId))
                        _toastEvent.postValue(SingleEvent(it.errorMessage))
                    }
                    else -> {}
                }
            }
        }
    }

    fun createReport(eventId: Long) {
        Log.d("MeetDebLog_EventsViewModel", "Creating event report with event ID $eventId ...")
        viewModelScope.launch {
            restClient.createReport(eventId).let {
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
            restClient.deleteReport(eventId).let {
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

    fun deleteEvent(eventId: Long) {
        Log.d("MeetDebLog_EventsViewModel", "Deleting event with ID $eventId ...")
        viewModelScope.launch {
            restClient.deleteEvent(eventId).let {
                when (it.status) {
                    Status.SUCCESS -> it.data?.withId?.let { eventId -> removeEventFromList(eventId) }
                    Status.ERROR -> _toastEvent.postValue(SingleEvent(it.errorMessage))
                    else -> {}
                }
            }
        }
    }

    private fun addEventsToList(events: List<Event>, page: Int) {
        if (page > 1) _events.postValue(_events.value?.union(events)?.toList())
        else _events.postValue(events)
        if (events.isNotEmpty()) lastPage = page
    }

    private fun updateEventInList(event: Event) {
        _events.postValue(_events.value?.map { if (it.id == event.id) event else it })
    }

    private fun removeEventFromList(eventId: Long) {
        _events.postValue(_events.value?.filterNot { it.id == eventId })
    }

    private fun addReportToEvent(eventId: Long) {
        _events.value?.find { it.id == eventId }?.reported = true
    }

    private fun removeReportFromEvent(eventId: Long) {
        _events.value?.find { it.id == eventId }?.reported = false
    }
}
