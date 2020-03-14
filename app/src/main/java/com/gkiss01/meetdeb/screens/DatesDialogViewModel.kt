package com.gkiss01.meetdeb.screens

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.data.fastadapter.Date
import com.gkiss01.meetdeb.data.fastadapter.Event

class DatesDialogViewModel : ViewModel() {
    val dates = MutableLiveData<List<Date>>()
    var isLoading = false
    lateinit var event: Event

    fun setDates(dateList: List<Date>) {
        dates.value = dateList
    }

    fun deleteDate(dateId: Long) {
        dates.value = dates.value!!.filterNot { it.id == dateId }
    }

    fun createVote(dateId: Long) {
        if (!isLoading) {
            MainActivity.instance.createVote(dateId)
            isLoading = true
        }
    }
}
