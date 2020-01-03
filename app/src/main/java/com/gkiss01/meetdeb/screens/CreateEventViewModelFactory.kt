package com.gkiss01.meetdeb.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gkiss01.meetdeb.data.EventEntryDao

class CreateEventViewModelFactory(
    private val dataSource: EventEntryDao,
    private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateEventViewModel::class.java)) {
            return CreateEventViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}