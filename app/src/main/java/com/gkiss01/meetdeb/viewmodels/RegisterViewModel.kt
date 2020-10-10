package com.gkiss01.meetdeb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.User
import com.gkiss01.meetdeb.data.request.UserRequest
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.RestClient
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.utils.SingleEvent
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val registerModule = module {
    viewModel { RegisterViewModel(get(), get()) }
}

class RegisterViewModel(private val restClient: RestClient, private val moshi: Moshi): ViewModel() {
    private val _toastEvent = MutableLiveData<SingleEvent<Any>>()
    val toastEvent: LiveData<SingleEvent<Any>>
        get() = _toastEvent

    private val _currentlyRegistering = MutableLiveData<Boolean>()
    val currentlyRegistering: LiveData<Boolean>
        get() = _currentlyRegistering

    private val _operationSuccessful = MutableLiveData<SingleEvent<User>>()
    val operationSuccessful: LiveData<SingleEvent<User>>
        get() = _operationSuccessful

    fun createUser(email: String, password: String, name: String) {
        if (_currentlyRegistering.value == true) return
        val userRequest = UserRequest(email, password, name)
        val json = moshi.adapter(UserRequest::class.java).toJson(userRequest)
        val user = json.toRequestBody("application/json".toMediaTypeOrNull())
        createUser(user)
    }

    private fun createUser(user: RequestBody) {
        if (_currentlyRegistering.value == true) return
        Log.d("MeetDebLog_RegisterViewModel", "Creating user ...")
        _currentlyRegistering.postValue(true)
        viewModelScope.launch {
            restClient.createUser(user).let {
                _currentlyRegistering.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { user -> _operationSuccessful.postValue(SingleEvent(user)) }
                    Status.ERROR -> {
                        if (it.errorCode != ErrorCodes.USER_DISABLED_OR_NOT_VALID)
                            _toastEvent.postValue(SingleEvent(it.errorMessage))
                    }
                    else -> {}
                }
            }
        }
    }

    init {
        _currentlyRegistering.value = false
    }
}
