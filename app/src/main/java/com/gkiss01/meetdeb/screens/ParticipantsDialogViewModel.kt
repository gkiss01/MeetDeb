package com.gkiss01.meetdeb.screens

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.data.fastadapter.Participant

class ParticipantsDialogViewModel : ViewModel() {
    val participants = MutableLiveData<List<Participant>>()

    fun setParticipants(participantList: List<Participant>) {
        participants.value = participantList
    }
}
