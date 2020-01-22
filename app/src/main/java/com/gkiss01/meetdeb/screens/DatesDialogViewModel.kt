package com.gkiss01.meetdeb.screens

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.data.Date

class DatesDialogViewModel : ViewModel() {
    val dates = MutableLiveData<List<Date>>()
}
