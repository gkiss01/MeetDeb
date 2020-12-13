package com.gkiss01.meetdeb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.remote.response.Event
import com.gkiss01.meetdeb.data.remote.response.Participant
import com.gkiss01.meetdeb.network.api.RestClient
import com.gkiss01.meetdeb.network.common.Resource.Status
import com.gkiss01.meetdeb.utils.SingleEvent
import kotlinx.coroutines.launch

class ParticipantsViewModel(private val restClient: RestClient) : ViewModel() {
    lateinit var event: Event
    fun isEventInitialized() = ::event.isInitialized

    private val _toastEvent = MutableLiveData<SingleEvent<Any>>()
    val toastEvent: LiveData<SingleEvent<Any>>
        get() = _toastEvent

    private val _headerCurrentlyNeeded = MutableLiveData<Boolean>()
    val headerCurrentlyNeeded: LiveData<Boolean>
        get() = _headerCurrentlyNeeded

    private var _participants = MutableLiveData<List<Participant>>()
    val participants: LiveData<List<Participant>>
        get() = _participants

    fun getParticipants() {
        if (_headerCurrentlyNeeded.value == true) return
        Log.d("Logger_ParticipantsVM", "Participants are loading with event ID ${event.id} ...")
        _headerCurrentlyNeeded.postValue(true)
        viewModelScope.launch {
            restClient.getParticipants(event.id).let {
                _headerCurrentlyNeeded.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { participants -> _participants.postValue(participants) }
                    Status.ERROR -> _toastEvent.postValue(SingleEvent(it.errorMessage))
                    else -> {}
                }
            }
        }
    }
}
