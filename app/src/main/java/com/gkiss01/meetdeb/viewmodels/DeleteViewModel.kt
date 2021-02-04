package com.gkiss01.meetdeb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.network.api.RestClient
import com.gkiss01.meetdeb.network.common.Resource.Status
import com.gkiss01.meetdeb.utils.SingleEvent
import com.gkiss01.meetdeb.utils.VoidEvent
import com.gkiss01.meetdeb.utils.postEvent
import kotlinx.coroutines.launch

class DeleteViewModel(private val restClient: RestClient): ViewModel() {
    private val _toastEvent = MutableLiveData<SingleEvent<Any>>()
    val toastEvent: LiveData<SingleEvent<Any>>
        get() = _toastEvent

    private val _currentlyDeleting = MutableLiveData<Boolean>()
    val currentlyDeleting: LiveData<Boolean>
        get() = _currentlyDeleting

    private val _operationSuccessful = MutableLiveData<VoidEvent>()
    val operationSuccessful: LiveData<VoidEvent>
        get() = _operationSuccessful

    fun deleteUser() {
        if (_currentlyDeleting.value == true) return
        Log.d("Logger_DeleteVM", "Deleting user ...")
        _currentlyDeleting.postValue(true)
        viewModelScope.launch {
            restClient.deleteUser().let {
                _currentlyDeleting.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { _operationSuccessful.postEvent() }
                    Status.ERROR -> _toastEvent.postEvent(it.error?.localizedDescription)
                }
            }
        }
    }

    init {
        _currentlyDeleting.value = false
    }
}
