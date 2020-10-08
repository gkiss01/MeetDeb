package com.gkiss01.meetdeb.viewmodels

import androidx.lifecycle.*
import com.gkiss01.meetdeb.data.fastadapter.Date
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.network.Resource
import com.gkiss01.meetdeb.network.RestClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.threeten.bp.OffsetDateTime

val datesModule = module {
    factory { (basic: String) -> DatesViewModel(get(), basic) }
}

class DatesViewModel(private val restClient: RestClient, private val basic: String) : ViewModel() {
    private var isLoading = false
    lateinit var event: Event
    fun isEventInitialized() = ::event.isInitialized

    private var _dates = MutableLiveData<Resource<List<Date>>>()
    val dates: LiveData<Resource<List<Date>>>
        get() = _dates

    fun getDates() {
        _dates.postValue(Resource.loading(null))
        viewModelScope.launch {
            _dates.postValue(restClient.getDates(basic, event.id))
        }
    }

    fun createDate(date: OffsetDateTime) {
        _dates.postValue(Resource.loading(null))
        viewModelScope.launch {
            _dates.postValue(restClient.createDate(basic, event.id, date))
        }
    }

    fun deleteDate(dateId: Long) = liveData {
        emit(Resource.loading(null))
        emit(restClient.deleteDate(basic, dateId))
    }

    fun changeVote(dateId: Long) {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch {
            _dates.postValue(restClient.changeVote(basic, dateId))
            isLoading = false
        }
    }

    fun isLoadingActive() = this.isLoading

    fun removeDateFromList(dateId: Long) {
        _dates.postValue(Resource.success(_dates.value?.data?.filterNot { it.id == dateId }))
    }
}
