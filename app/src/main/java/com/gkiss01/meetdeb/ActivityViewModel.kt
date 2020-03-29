package com.gkiss01.meetdeb

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.data.User
import okhttp3.Credentials

class ActivityViewModel : ViewModel() {
    val activeUser = MutableLiveData<User>()
    lateinit var basic: String

    fun recalculateBasic(username: String, password: String) {
        basic = Credentials.basic(username, password)
    }

    fun recalculateBasic(password: String) {
        basic = Credentials.basic(activeUser.value!!.email, password)
    }
}