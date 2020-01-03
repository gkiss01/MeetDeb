package com.gkiss01.meetdeb.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gkiss01.meetdeb.data.EventEntryDao
import com.gkiss01.meetdeb.data.ParticipantEntryDao

class EventsViewModelFactory(
    private val eventDataSource: EventEntryDao,
    private val participantDataSource: ParticipantEntryDao,
    private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventsViewModel::class.java)) {
            return EventsViewModel(eventDataSource, participantDataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}