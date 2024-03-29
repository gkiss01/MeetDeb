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
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class UpdateViewModel(private val restClient: RestClient, private val moshi: Moshi, private val authManager: AuthManager): ViewModel() {
    var userLocal: UserRequest = UserRequest()
        private set

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
        updateUser(getAuthToken(), prepareUser())
    }

    private fun updateUser(basic: String, user: RequestBody) {
        if (_currentlyUpdating.value == true) return
        _currentlyUpdating.postValue(true)
        Log.d("Logger_UpdateVM", "Updating user ...")

        viewModelScope.launch {
            restClient.updateUser(basic, user).let {
                _currentlyUpdating.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { user -> _operationSuccessful.postEvent(user) }
                    Status.ERROR -> {
                        val message = if (it.error?.code == ErrorCode.USER_DISABLED_OR_NOT_VALID) R.string.invalid_current_password else it.error?.localizedDescription
                        _toastEvent.postEvent(message)
                    }
                }
            }
        }
    }

    private fun getAuthToken(): String {
        val email = authManager.getCredential(AuthManager.CredentialType.EMAIL) ?: ""
        val password = userLocal.name ?: ""
        return Credentials.basic(email, password)
    }

    private fun prepareUser(): RequestBody {
        val userRequest = UserRequest(userLocal.email, userLocal.password, null)
        val json = moshi.adapter(UserRequest::class.java).toJson(userRequest)
        return json.toRequestBody("application/json".toMediaTypeOrNull())
    }

    init {
        _currentlyUpdating.value = false
    }
}
