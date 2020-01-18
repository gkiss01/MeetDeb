package com.gkiss01.meetdeb.screens

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.data.Event

class EventsViewModel : ViewModel() {
    val events = MutableLiveData<List<Event>>()

    init {
        MainActivity.instance.getEvents()
    }
}
