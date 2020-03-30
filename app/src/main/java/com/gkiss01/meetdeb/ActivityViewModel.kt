package com.gkiss01.meetdeb

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.data.User
import okhttp3.Credentials

class ActivityViewModel : ViewModel() {
    val activeUser = MutableLiveData<User>()
    lateinit var password: String
    var tempPassword: String? = null

    lateinit var basic: String

//    val basic: LiveData<String> = Transformations.map(activeUser) {
//        Credentials.basic(it.email, password)
//    }

    fun calculateBasic() {
        basic = Credentials.basic(activeUser.value!!.email, password)
    }
}