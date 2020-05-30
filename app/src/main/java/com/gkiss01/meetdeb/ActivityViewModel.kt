package com.gkiss01.meetdeb

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.gkiss01.meetdeb.data.User
import com.gkiss01.meetdeb.data.apirequest.UserRequest
import com.gkiss01.meetdeb.network.Resource
import com.gkiss01.meetdeb.network.RestClient
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val activityModule = module {
    factory { ActivityViewModel(get(), get(), androidApplication()) }
}

class ActivityViewModel(private val moshi: Moshi, private val restClient: RestClient, private val application: Application) : ViewModel() {
    private var username = application.getSavedUsername()
    private var password = application.getSavedPassword()
    private lateinit var basic: String

    private var _activeUser = MutableLiveData<Resource<User>>()
    val activeUser: LiveData<Resource<User>>
        get() = _activeUser

    fun getCurrentUser(basic: String = Credentials.basic(username, password)) {
        this.basic = basic

        _activeUser.postValue(Resource.loading(null))
        viewModelScope.launch {
            _activeUser.postValue(restClient.checkUser(basic))
        }
    }

    fun createUser(email: String, password: String, name: String) {
        val userRequest = UserRequest(email, password, name)
        val json = moshi.adapter(UserRequest::class.java).toJson(userRequest)
        val user = json.toRequestBody("application/json".toMediaTypeOrNull())

        _activeUser.postValue(Resource.loading(null))
        viewModelScope.launch {
            _activeUser.postValue(restClient.createUser(user))
        }
    }

    fun updateUser(currentPassword: String, email: String?, password: String?) = liveData(Dispatchers.IO) {
        val basic = Credentials.basic(activeUser.value?.data?.email ?: "", currentPassword)

        val userRequest = UserRequest(email, password, null)
        val json = moshi.adapter(UserRequest::class.java).toJson(userRequest)
        val user = json.toRequestBody("application/json".toMediaTypeOrNull())

        emit(Resource.loading(null))
        emit(restClient.updateUser(basic, user))
    }

    fun deleteUser() = liveData(Dispatchers.IO) {
        emit(Resource.loading(null))
        emit(restClient.deleteUser(basic))
    }

    fun setActiveUser(user: User) {
        _activeUser.postValue(Resource.success(user))
    }

    fun setUserCredentials(username: String?, password: String?) {
        username?.let { this.username = it }
        password?.let { this.password = it }
        basic = Credentials.basic(this.username, this.password)
        application.setSavedUser(this.username, this.password)
    }

    fun resetLiveData() {
        _activeUser.postValue(Resource.pending(null))
    }

    fun resetUserCredentials() {
        username = ""
        password = ""
        basic = ""
        application.setSavedUser("",  "")
    }

    fun getBasic() = basic
}

fun Context.getSavedUsername(default: String = "unknown"): String {
    val sharedPref = this.getSharedPreferences("BASIC_AUTH_PREFS", Context.MODE_PRIVATE)
    return sharedPref.getString("OPTION_EMAIL", default)!!
}

fun Context.getSavedPassword(default: String = "unknown"): String {
    val sharedPref = this.getSharedPreferences("BASIC_AUTH_PREFS", Context.MODE_PRIVATE)
    return sharedPref.getString("OPTION_PASSWORD", default)!!
}

fun Context.setSavedUser(username: String, password: String) {
    val sharedPref = this.getSharedPreferences("BASIC_AUTH_PREFS", Context.MODE_PRIVATE)
    sharedPref.edit().putString("OPTION_EMAIL", username).putString("OPTION_PASSWORD", password).apply()
}