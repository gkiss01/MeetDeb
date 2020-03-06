package com.gkiss01.meetdeb.screens

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.data.fastadapter.Date

class DatesDialogViewModel : ViewModel() {
    val dates = MutableLiveData<List<Date>>()
    var isLoading = false
    var votesChanged = false
    var eventId = Long.MIN_VALUE

    fun addVote(dateId: Long) {
        if (!isLoading) {
            MainActivity.instance.addVote(dateId)
            isLoading = true
        }
    }

    fun setDates(dateList: List<Date>) {
        dates.value = dateList
    }
}
