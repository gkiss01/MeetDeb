package com.gkiss01.meetdeb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.fastadapter.Date
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.network.Resource
import com.gkiss01.meetdeb.network.RestClient
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.utils.SingleEvent
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.threeten.bp.OffsetDateTime

val datesModule = module {
    factory { DatesViewModel(get()) }
}

class DatesViewModel(private val restClient: RestClient) : ViewModel() {
    private var isLoading = false
    lateinit var event: Event
    fun isEventInitialized() = ::event.isInitialized

    private val _toastEvent = MutableLiveData<SingleEvent<String>>()
    val toastEvent: LiveData<SingleEvent<String>>
        get() = _toastEvent

    private var _dates = MutableLiveData<Resource<List<Date>>>()
    val dates: LiveData<Resource<List<Date>>>
        get() = _dates

    fun getDates() {
        _dates.postValue(Resource.loading(null))
        viewModelScope.launch {
            _dates.postValue(restClient.getDates(event.id))
        }
    }

    fun createDate(date: OffsetDateTime) {
        _dates.postValue(Resource.loading(null))
        viewModelScope.launch {
            _dates.postValue(restClient.createDate(event.id, date))
        }
    }

    fun deleteDate(dateId: Long) {
        Log.d("MeetDebLog_DatesDialogViewModel", "Deleting date with ID $dateId ...")
        viewModelScope.launch {
            restClient.deleteDate(dateId).let {
                when (it.status) {
                    Status.SUCCESS -> it.data?.withId?.let { dateId -> removeDateFromList(dateId) }
                    Status.ERROR -> _toastEvent.postValue(SingleEvent(it.errorMessage))
                    else -> {}
                }
            }
        }
    }

    fun changeVote(dateId: Long) {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch {
            _dates.postValue(restClient.changeVote(dateId))
            isLoading = false
        }
    }

    fun isLoadingActive() = this.isLoading

    private fun removeDateFromList(dateId: Long) {
        _dates.postValue(Resource.success(_dates.value?.data?.filterNot { it.id == dateId }))
    }
}
