package com.gkiss01.meetdeb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.remote.request.UserRequest
import com.gkiss01.meetdeb.data.remote.response.User
import com.gkiss01.meetdeb.network.api.RestClient
import com.gkiss01.meetdeb.network.common.Error.ErrorCode
import com.gkiss01.meetdeb.network.common.Resource.Status
import com.gkiss01.meetdeb.utils.SingleEvent
import com.gkiss01.meetdeb.utils.postEvent
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    viewModel { RegisterViewModel(get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { LoadingViewModel(get()) }
    viewModel { UpdateViewModel(get(), get(), get()) }
    viewModel { DeleteViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
}

class RegisterViewModel(private val restClient: RestClient, private val moshi: Moshi): ViewModel() {
    var userLocal: UserRequest = UserRequest()
        private set

    private val _toastEvent = MutableLiveData<SingleEvent<Any>>()
    val toastEvent: LiveData<SingleEvent<Any>>
        get() = _toastEvent

    private val _currentlyRegistering = MutableLiveData<Boolean>()
    val currentlyRegistering: LiveData<Boolean>
        get() = _currentlyRegistering

    private val _operationSuccessful = MutableLiveData<SingleEvent<User>>()
    val operationSuccessful: LiveData<SingleEvent<User>>
        get() = _operationSuccessful

    fun createUser() {
        if (_currentlyRegistering.value == true) return
        Log.d("Logger_RegisterVM", "Creating user ...")
        val json = moshi.adapter(UserRequest::class.java).toJson(userLocal)
        val user = json.toRequestBody("application/json".toMediaTypeOrNull())
        _currentlyRegistering.postValue(true)
        viewModelScope.launch {
            restClient.createUser(user).let {
                _currentlyRegistering.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { user -> _operationSuccessful.postEvent(user) }
                    Status.ERROR -> {
                        if (it.error?.code != ErrorCode.USER_DISABLED_OR_NOT_VALID)
                            _toastEvent.postEvent(it.error?.localizedDescription)
                    }
                }
            }
        }
    }

    init {
        _currentlyRegistering.value = false
    }
}
