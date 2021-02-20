package com.gkiss01.meetdeb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.remote.response.EventSummary
import com.gkiss01.meetdeb.network.api.RestClient
import com.gkiss01.meetdeb.network.common.Resource.Status
import com.gkiss01.meetdeb.utils.SingleEvent
import com.gkiss01.meetdeb.utils.postEvent
import kotlinx.coroutines.launch

class ProfileViewModel(private val restClient: RestClient): ViewModel() {
    private val _toastEvent = MutableLiveData<SingleEvent<Any>>()
    val toastEvent: LiveData<SingleEvent<Any>>
        get() = _toastEvent

    private val _currentlyLoading = MutableLiveData<Boolean>()

    private val _eventsSummary = MutableLiveData<EventSummary>()
    val eventsSummary: LiveData<EventSummary>
        get() = _eventsSummary

    fun getEventsSummary() {
        if (_currentlyLoading.value == true) return
        _currentlyLoading.postValue(true)
        Log.d("Logger_ProfileVM", "Loading events summary ...")

        viewModelScope.launch {
            restClient.getEventsSummary().let {
                _currentlyLoading.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { summary -> _eventsSummary.postValue(summary) }
                    Status.ERROR -> _toastEvent.postEvent(it.error?.localizedDescription)
                }
            }
        }
    }

    init {
        _currentlyLoading.value = false
    }
}
