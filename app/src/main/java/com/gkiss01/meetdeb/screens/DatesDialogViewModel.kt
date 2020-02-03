package com.gkiss01.meetdeb.screens

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.data.Date

class DatesDialogViewModel : ViewModel() {
    val dates = MutableLiveData<List<Date>>()
    val isLoading = MutableLiveData<Boolean>()
    val votesChanged = MutableLiveData<Boolean>()

    init {
        isLoading.value = false
        votesChanged.value = false
    }

    fun addVote(dateId: Long) {
        if (isLoading.value!!) return
        isLoading.value = true
        MainActivity.instance.addVote(dateId)
    }
}
