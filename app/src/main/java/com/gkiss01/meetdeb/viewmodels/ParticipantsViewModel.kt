package com.gkiss01.meetdeb.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.data.fastadapter.Participant
import com.gkiss01.meetdeb.network.Resource
import com.gkiss01.meetdeb.network.RestClient
import kotlinx.coroutines.launch
import org.koin.dsl.module

val participantsModule = module {
    factory { (basic: String) -> ParticipantsViewModel(get(), basic) }
}

class ParticipantsViewModel(private val restClient: RestClient, private val basic: String) : ViewModel() {
    lateinit var event: Event
    fun isEventInitialized() = ::event.isInitialized

    private var _participants = MutableLiveData<Resource<List<Participant>>>()
    val participants: LiveData<Resource<List<Participant>>>
        get() = _participants

    fun getParticipants() {
        _participants.postValue(Resource.loading(null))
        viewModelScope.launch {
            _participants.postValue(restClient.getParticipants(basic, event.id))
        }
    }
}
