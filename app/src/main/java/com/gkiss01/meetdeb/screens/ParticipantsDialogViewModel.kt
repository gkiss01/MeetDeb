package com.gkiss01.meetdeb.screens

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.data.Participant

class ParticipantsDialogViewModel : ViewModel() {
    val participants = MutableLiveData<List<Participant>>()
}
