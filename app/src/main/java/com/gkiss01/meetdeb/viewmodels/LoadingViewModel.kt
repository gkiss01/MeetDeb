package com.gkiss01.meetdeb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.remote.response.User
import com.gkiss01.meetdeb.network.api.RestClient
import com.gkiss01.meetdeb.network.common.ErrorCodes
import com.gkiss01.meetdeb.network.common.Resource.Status
import com.gkiss01.meetdeb.utils.SingleEvent
import com.gkiss01.meetdeb.utils.VoidEvent
import kotlinx.coroutines.launch

class LoadingViewModel(private val restClient: RestClient): ViewModel() {
    private val _toastEvent = MutableLiveData<SingleEvent<Any>>()
    val toastEvent: LiveData<SingleEvent<Any>>
        get() = _toastEvent

    private val _currentlyLoggingIn = MutableLiveData<Boolean>()
    val currentlyLoggingIn: LiveData<Boolean>
        get() = _currentlyLoggingIn

    private val _operationSuccessful = MutableLiveData<SingleEvent<User>>()
    val operationSuccessful: LiveData<SingleEvent<User>>
        get() = _operationSuccessful

    private val _operationUnsuccessful = MutableLiveData<VoidEvent>()
    val operationUnsuccessful: LiveData<VoidEvent>
        get() = _operationUnsuccessful

    fun checkUser() {
        if (_currentlyLoggingIn.value == true) return
        Log.d("Logger_LoadingVM", "Checking user ...")
        _currentlyLoggingIn.postValue(true)
        viewModelScope.launch {
            restClient.checkUser().let {
                //_currentlyLoggingIn.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { user -> _operationSuccessful.postValue(SingleEvent(user)) }
                    Status.ERROR -> {
                        _operationUnsuccessful.postValue(VoidEvent())
                        if (it.errorCode != ErrorCodes.USER_DISABLED_OR_NOT_VALID)
                            _toastEvent.postValue(SingleEvent(it.errorMessage))
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
