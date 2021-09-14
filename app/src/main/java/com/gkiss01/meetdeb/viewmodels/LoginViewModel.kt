package com.gkiss01.meetdeb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.remote.request.UserRequest
import com.gkiss01.meetdeb.data.remote.response.User
import com.gkiss01.meetdeb.network.api.RestClient
import com.gkiss01.meetdeb.network.common.ErrorCode
import com.gkiss01.meetdeb.network.common.Resource.Status
import com.gkiss01.meetdeb.utils.AuthManager
import com.gkiss01.meetdeb.utils.SingleEvent
import com.gkiss01.meetdeb.utils.postEvent
import kotlinx.coroutines.launch
import okhttp3.Credentials

class LoginViewModel(private val restClient: RestClient, private val authManager: AuthManager): ViewModel() {
    var userLocal: UserRequest = UserRequest()
        private set

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
        updateAuthToken()
        checkUser()
    }

    private fun checkUser() {
        if (_currentlyLoggingIn.value == true) return
        _currentlyLoggingIn.postValue(true)
        Log.d("Logger_LoginVM", "Logging in ...")

        viewModelScope.launch {
            restClient.checkUser().let {
                _currentlyLoggingIn.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { user -> _operationSuccessful.postEvent(user) }
                    Status.ERROR -> {
                        val message = if (it.error?.code == ErrorCode.USER_DISABLED_OR_NOT_VALID) R.string.invalid_credentials else it.error?.localizedDescription
                        _toastEvent.postEvent(message)
                        authManager.setAuthToken(null)
                    }
                }
            }
        }
    }

    private fun updateAuthToken() {
        userLocal.email?.let { email ->
            userLocal.password?.let { password ->
                authManager.setAuthToken(Credentials.basic(email, password))
            }
        }
    }

    init {
        _currentlyLoggingIn.value = false
    }
}
