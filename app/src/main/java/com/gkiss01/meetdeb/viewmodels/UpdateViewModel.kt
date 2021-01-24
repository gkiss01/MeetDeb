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
import com.gkiss01.meetdeb.network.common.Resource.ErrorCode
import com.gkiss01.meetdeb.network.common.Resource.Status
import com.gkiss01.meetdeb.utils.CredentialType
import com.gkiss01.meetdeb.utils.SingleEvent
import com.gkiss01.meetdeb.utils.getCurrentCredential
import com.gkiss01.meetdeb.utils.postEvent
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class UpdateViewModel(private val restClient: RestClient, private val moshi: Moshi, private val application: Application): ViewModel() {
    lateinit var userLocal: UserRequest
    fun isUserInitialized() = ::userLocal.isInitialized

    private val _toastEvent = MutableLiveData<SingleEvent<Any>>()
    val toastEvent: LiveData<SingleEvent<Any>>
        get() = _toastEvent

    private val _currentlyUpdating = MutableLiveData<Boolean>()
    val currentlyUpdating: LiveData<Boolean>
        get() = _currentlyUpdating

    private val _operationSuccessful = MutableLiveData<SingleEvent<User>>()
    val operationSuccessful: LiveData<SingleEvent<User>>
        get() = _operationSuccessful

    fun updateUser() {
        if (_currentlyUpdating.value == true) return
        Log.d("Logger_UpdateVM", "Updating user ...")
        val email = application.getCurrentCredential(CredentialType.EMAIL)
        val password = userLocal.name ?: ""
        val basic = Credentials.basic(email, password)

        val userRequest = UserRequest(userLocal.email, userLocal.password, null)
        val json = moshi.adapter(UserRequest::class.java).toJson(userRequest)
        val user = json.toRequestBody("application/json".toMediaTypeOrNull())
        _currentlyUpdating.postValue(true)
        viewModelScope.launch {
            restClient.updateUser(basic, user).let {
                _currentlyUpdating.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { user -> _operationSuccessful.postEvent(user) }
                    Status.ERROR -> {
                        val message = if (it.errorCode == ErrorCode.USER_DISABLED_OR_NOT_VALID) R.string.invalid_current_password else it.errorMessage
                        _toastEvent.postEvent(message)
                    }
                }
            }
        }
    }

    init {
        _currentlyUpdating.value = false
    }
}
