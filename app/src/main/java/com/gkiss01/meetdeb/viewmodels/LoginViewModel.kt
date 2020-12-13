package com.gkiss01.meetdeb.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.remote.request.UserRequest
import com.gkiss01.meetdeb.data.remote.response.User
import com.gkiss01.meetdeb.network.api.RestClient
import com.gkiss01.meetdeb.network.common.ErrorCodes
import com.gkiss01.meetdeb.network.common.Resource.Status
import com.gkiss01.meetdeb.utils.SingleEvent
import com.gkiss01.meetdeb.utils.setAuthToken
import kotlinx.coroutines.launch
import okhttp3.Credentials

class LoginViewModel(private val restClient: RestClient, private val application: Application): ViewModel() {
    lateinit var userLocal: UserRequest
    fun isUserInitialized() = ::userLocal.isInitialized

    private val _toastEvent = MutableLiveData<SingleEvent<Any>>()
    val toastEvent: LiveData<SingleEvent<Any>>
        get() = _toastEvent

    private val _currentlyLoggingIn = MutableLiveData<Boolean>()
    val currentlyLoggingIn: LiveData<Boolean>
        get() = _currentlyLoggingIn

    private val _operationSuccessful = MutableLiveData<SingleEvent<User>>()
    val operationSuccessful: LiveData<SingleEvent<User>>
        get() = _operationSuccessful

    fun loginUser() {
        if (_currentlyLoggingIn.value == true) return
        Log.d("Logger_LoginVM", "Logging in ...")
        userLocal.email?.let { email ->
            userLocal.password?.let { password ->
                application.setAuthToken(Credentials.basic(email, password))
            }
        }
        _currentlyLoggingIn.postValue(true)
        viewModelScope.launch {
            restClient.checkUser().let {
                _currentlyLoggingIn.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { user -> _operationSuccessful.postValue(SingleEvent(user)) }
                    Status.ERROR -> {
                        val message = if (it.errorCode == ErrorCodes.USER_DISABLED_OR_NOT_VALID) R.string.invalid_credentials else it.errorMessage
                        _toastEvent.postValue(SingleEvent(message))
                    }
                    else -> {}
                }
            }
        }
    }

    init {
        _currentlyLoggingIn.value = false
    }
}
