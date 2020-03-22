package com.gkiss01.meetdeb.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.data.fastadapter.Participant

class ParticipantsViewModel : ViewModel() {
    val participants = MutableLiveData<List<Participant>>()

    fun setParticipants(participantList: List<Participant>) {
        participants.value = participantList
    }
}
