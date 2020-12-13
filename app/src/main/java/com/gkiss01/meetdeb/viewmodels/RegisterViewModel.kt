package com.gkiss01.meetdeb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.remote.request.UserRequest
import com.gkiss01.meetdeb.data.remote.response.User
import com.gkiss01.meetdeb.network.api.RestClient
import com.gkiss01.meetdeb.network.common.ErrorCodes
import com.gkiss01.meetdeb.network.common.Resource.Status
import com.gkiss01.meetdeb.utils.SingleEvent
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    viewModel { RegisterViewModel(get(), get()) }
    viewModel { LoginViewModel(get(), androidApplication()) }
    viewModel { LoadingViewModel(get()) }
    viewModel { UpdateViewModel(get(), get(), androidApplication()) }
    viewModel { DeleteViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
}

class RegisterViewModel(private val restClient: RestClient, private val moshi: Moshi): ViewModel() {
    lateinit var userLocal: UserRequest
    fun isUserInitialized() = ::userLocal.isInitialized

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
