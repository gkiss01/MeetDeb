package com.gkiss01.meetdeb.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.network.WebApi
import kotlinx.coroutines.launch
import okhttp3.Credentials

class EventsViewModel(application: Application): AndroidViewModel(application) {

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
