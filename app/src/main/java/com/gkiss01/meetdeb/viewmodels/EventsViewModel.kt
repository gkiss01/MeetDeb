package com.gkiss01.meetdeb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.remote.response.Event
import com.gkiss01.meetdeb.network.api.RestClient
import com.gkiss01.meetdeb.network.common.Resource.Status
import com.gkiss01.meetdeb.utils.SingleEvent
import com.gkiss01.meetdeb.utils.postEvent
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val eventsModule = module {
    viewModel { EventsViewModel(get()) }
    viewModel { EventCreateViewModel(get(), get(), get()) }
    viewModel { DatesViewModel(get()) }
    viewModel { ParticipantsViewModel(get()) }
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
        _footerCurrentlyNeeded.postValue(true)
        Log.d("Logger_EventsVM", "Events are loading...")

        viewModelScope.launch {
            restClient.getEvents(page).let {
                _footerCurrentlyNeeded.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { events -> addEventsToList(events, page) }
                    Status.ERROR -> _toastEvent.postEvent(it.error?.localizedDescription)
                }
            }
        }
    }

    fun updateEvent(eventId: Long) {
        if (_itemCurrentlyUpdating.value != null) return
        _itemCurrentlyUpdating.postValue(Pair(Event.UpdatingType.VOTE, eventId))
        Log.d("Logger_EventsVM", "Updating event with ID $eventId ...")

        viewModelScope.launch {
            restClient.getEvent(eventId).let {
                _itemCurrentlyUpdating.postValue(null)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { event -> updateEventInList(event) }
                    Status.ERROR -> {
                        _updateItemEvent.postEvent(eventId)
                        _toastEvent.postEvent(it.error?.localizedDescription)
                    }
                }
            }
        }
    }

    fun modifyParticipation(eventId: Long) {
        if (_itemCurrentlyUpdating.value != null) return
        _itemCurrentlyUpdating.postValue(Pair(Event.UpdatingType.PARTICIPATION, eventId))
        Log.d("Logger_EventsVM", "Modifying participation with event ID $eventId ...")

        viewModelScope.launch {
            restClient.modifyParticipation(eventId).let {
                _itemCurrentlyUpdating.postValue(null)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { event -> updateEventInList(event) }
                    Status.ERROR -> {
                        _updateItemEvent.postEvent(eventId)
                        _toastEvent.postEvent(it.error?.localizedDescription)
                    }
                }
            }
        }
    }

    fun createReport(eventId: Long) {
        Log.d("Logger_EventsVM", "Creating event report with event ID $eventId ...")

        viewModelScope.launch {
            restClient.createReport(eventId).let {
                when (it.status) {
                    Status.SUCCESS -> it.data?.withId?.let { eventId ->
                        addReportToEvent(eventId)
                        _updateItemEvent.postEvent(eventId)
                        _toastEvent.postEvent(R.string.event_reported)
                    }
                    Status.ERROR -> _toastEvent.postEvent(it.error?.localizedDescription)
                }
            }
        }
    }

    fun deleteReport(eventId: Long) {
        Log.d("Logger_EventsVM", "Deleting event report with event ID $eventId ...")

        viewModelScope.launch {
            restClient.deleteReport(eventId).let {
                when (it.status) {
                    Status.SUCCESS -> it.data?.withId?.let { eventId ->
                        removeReportFromEvent(eventId)
                        _updateItemEvent.postEvent(eventId)
                        _toastEvent.postEvent(R.string.event_report_removed)
                    }
                    Status.ERROR -> _toastEvent.postEvent(it.error?.localizedDescription)
                }
            }
        }
    }

    fun deleteEvent(eventId: Long) {
        Log.d("Logger_EventsVM", "Deleting event with ID $eventId ...")

        viewModelScope.launch {
            restClient.deleteEvent(eventId).let {
                when (it.status) {
                    Status.SUCCESS -> it.data?.withId?.let { eventId -> removeEventFromList(eventId) }
                    Status.ERROR -> _toastEvent.postEvent(it.error?.localizedDescription)
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
