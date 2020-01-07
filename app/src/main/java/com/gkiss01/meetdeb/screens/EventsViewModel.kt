package com.gkiss01.meetdeb.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.gkiss01.meetdeb.data.EventEntry
import com.gkiss01.meetdeb.data.EventEntryDao
import com.gkiss01.meetdeb.data.ParticipantEntry
import com.gkiss01.meetdeb.data.ParticipantEntryDao
import com.gkiss01.meetdeb.network.WebApi
import com.gkiss01.meetdeb.network.data.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import kotlin.random.Random

class EventsViewModel(eventDatabaseDao: EventEntryDao,
                      private val participantDatabaseDao: ParticipantEntryDao,
                      application: Application): AndroidViewModel(application) {

    var userId: Long = Random.nextLong(0, 1000) // Különböző belépett felhasználók szimulálása

    private val _eventEntries = eventDatabaseDao.getEvents()
    val eventEntries = Transformations.switchMap(_eventEntries) { list ->
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            val data = list.map { element -> EventEntry(element) }
            emit(data)
        }
    }

    fun createParticipant(eventId: Int) {
        viewModelScope.launch {
            val participant = ParticipantEntry(participantId = userId, eventId = eventId)
            insert(participant)
        }
    }

    private suspend fun insert(participantEntry: ParticipantEntry) {
        withContext(Dispatchers.IO) {
            participantDatabaseDao.insert(participantEntry)
        }
    }

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>>
        get() = _events

    private fun getEvents() {
        viewModelScope.launch {
            val basic = Credentials.basic("gergokiss05@gmail.com", "asdasdasd")
            val getEventsDeferred = WebApi.retrofitService.getEventsAsync(basic)
            try {
                val listResult = getEventsDeferred.await()
                _events.value = listResult.events
                Log.d("EventsViewModel", "Osszesen lekerve ${listResult.events!!.size} esemeny!")
            }
            catch (e: Exception) {
                Log.d("EventsViewModel", "Hiba: ${e.message}")
            }
        }
    }

    init {
        getEvents()
    }
}
