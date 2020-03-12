package com.gkiss01.meetdeb

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.data.User

class ActivityViewModel : ViewModel() {
    val activeUser = MutableLiveData<User>()
    lateinit var basic: String
}