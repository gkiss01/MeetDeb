package com.gkiss01.meetdeb.screens

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.data.Event

class EventsViewModel : ViewModel() {
    val events = MutableLiveData<List<Event>>()
    val isLoading = MutableLiveData<Boolean>()
    private val currentPage = MutableLiveData<Int>()

    init {
        MainActivity.instance.getEvents()
        currentPage.value = 1
        isLoading.value = false
    }

    fun refreshEvents() {
        currentPage.value = 1
        MainActivity.instance.getEvents()
    }

    fun loadMoreEvents() {
        isLoading.value = true
        currentPage.value = currentPage.value!! + 1
        MainActivity.instance.getEvents(currentPage.value!!)
    }

    fun addEvents(eventList: List<Event>) {
        if (currentPage.value!! > 1)
            events.value = events.value?.union(eventList)?.toList()
        else
            events.value = eventList.toMutableList()
    }
}
