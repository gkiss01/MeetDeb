package com.gkiss01.meetdeb

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.User
import com.gkiss01.meetdeb.data.apirequest.UserRequest
import com.gkiss01.meetdeb.data.apirequest.UserRequestType
import com.gkiss01.meetdeb.network.Resource
import com.gkiss01.meetdeb.network.RestClient
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val activityModule = module {
    factory { ActivityViewModel(get(), get(), androidApplication()) }
}

class ActivityViewModel(private val moshi: Moshi, private val restClient: RestClient, application: Application) : ViewModel() {
    private val username = application.getSavedUsername()
    private val password = application.getSavedPassword()
    private lateinit var basic: String

    private var _activeUser = MutableLiveData<Resource<User>>()
    val activeUser: LiveData<Resource<User>>
        get() = _activeUser

//    val activeUser: LiveData<Resource<User>> = liveData(Dispatchers.IO) {
//        emit(Resource.loading(null))
//        emit(restClient.checkUserAsync(basic))
//    }

    fun getCurrentUser(basic: String = Credentials.basic(username, password)) {
        this.basic = basic

        _activeUser.postValue(Resource.loading(null))
        viewModelScope.launch {
            _activeUser.postValue(restClient.checkUserAsync(basic))
        }
    }

    fun createUser(email: String, password: String, name: String) {
        val userRequest = UserRequest(email, password, name, UserRequestType.Create.ordinal)
        val json = moshi.adapter(UserRequest::class.java).toJson(userRequest)
        val user = json.toRequestBody("application/json".toMediaTypeOrNull())

        _activeUser.postValue(Resource.loading(null))
        viewModelScope.launch {
            _activeUser.postValue(restClient.createUserAsync(user))
        }
    }
}

fun Context.getSavedUsername(default: String = "unknown"): String {
    val sharedPref = this.getSharedPreferences("BASIC_AUTH_PREFS", Context.MODE_PRIVATE)
    return sharedPref.getString("OPTION_EMAIL", default)!!
}

fun Context.getSavedPassword(default: String = "unknown"): String {
    val sharedPref = this.getSharedPreferences("BASIC_AUTH_PREFS", Context.MODE_PRIVATE)
    return sharedPref.getString("OPTION_PASSWORD", default)!!
}