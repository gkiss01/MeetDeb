package com.gkiss01.meetdeb.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class CreateEventViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateEventViewModel::class.java)) {
            return CreateEventViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}