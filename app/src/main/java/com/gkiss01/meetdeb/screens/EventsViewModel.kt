package com.gkiss01.meetdeb.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.EventEntry
import com.gkiss01.meetdeb.data.EventEntryDao
import com.gkiss01.meetdeb.data.ParticipantEntry
import com.gkiss01.meetdeb.data.ParticipantEntryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class EventsViewModel(private val eventDatabaseDao: EventEntryDao,
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
}
