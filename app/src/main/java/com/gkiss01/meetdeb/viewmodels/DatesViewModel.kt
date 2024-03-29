package com.gkiss01.meetdeb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.remote.response.Date
import com.gkiss01.meetdeb.data.remote.response.Event
import com.gkiss01.meetdeb.network.api.RestClient
import com.gkiss01.meetdeb.network.common.Resource.Status
import com.gkiss01.meetdeb.utils.SingleEvent
import com.gkiss01.meetdeb.utils.VoidEvent
import com.gkiss01.meetdeb.utils.format
import com.gkiss01.meetdeb.utils.postEvent
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime

class DatesViewModel(private val restClient: RestClient) : ViewModel() {
    lateinit var event: Event
    fun isEventInitialized() = ::event.isInitialized

    private val _toastEvent = MutableLiveData<SingleEvent<Any>>()
    val toastEvent: LiveData<SingleEvent<Any>>
        get() = _toastEvent

    private val _updateItemEvent = MutableLiveData<SingleEvent<Long>>()
    val updateItemEvent: LiveData<SingleEvent<Long>>
        get() = _updateItemEvent

    private val _itemCurrentlyUpdating = MutableLiveData<ItemUpdating?>()
    val itemCurrentlyUpdating: LiveData<ItemUpdating?>
        get() = _itemCurrentlyUpdating

    private val _headerCurrentlyNeeded = MutableLiveData<Boolean>()
    val headerCurrentlyNeeded: LiveData<Boolean>
        get() = _headerCurrentlyNeeded

    private val _itemCurrentlyAdding = MutableLiveData<OffsetDateTime?>()
    val itemCurrentlyAdding: LiveData<OffsetDateTime?>
        get() = _itemCurrentlyAdding

    private val _collapseFooter = MutableLiveData<VoidEvent>()
    val collapseFooter: LiveData<VoidEvent>
        get() = _collapseFooter

    private var _dates = MutableLiveData<List<Date>>()
    val dates: LiveData<List<Date>>
        get() = _dates

    fun getDates() {
        if (_headerCurrentlyNeeded.value == true) return
        _headerCurrentlyNeeded.postValue(true)
        Log.d("Logger_DatesVM", "Dates are loading with event ID ${event.id} ...")

        viewModelScope.launch {
            restClient.getDates(event.id).let {
                _headerCurrentlyNeeded.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { dates -> _dates.postValue(dates) }
                    Status.ERROR -> _toastEvent.postEvent(it.error?.localizedDescription)
                }
            }
        }
    }

    fun createDate(date: OffsetDateTime) {
        if (_itemCurrentlyAdding.value != null) return
        _itemCurrentlyAdding.postValue(date)
        Log.d("Logger_DatesVM", "Creating date ${date.format()} ...")

        viewModelScope.launch {
            restClient.createDate(event.id, date).let {
                _itemCurrentlyAdding.postValue(null)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { dates ->
                        _dates.postValue(dates)
                        _collapseFooter.postEvent()
                    }
                    Status.ERROR -> _toastEvent.postEvent(it.error?.localizedDescription)
                }
            }
        }
    }

    fun deleteDate(dateId: Long) {
        Log.d("Logger_DatesVM", "Deleting date with ID $dateId ...")

        viewModelScope.launch {
            restClient.deleteDate(dateId).let {
                when (it.status) {
                    Status.SUCCESS -> it.data?.withId?.let { dateId -> removeDateFromList(dateId) }
                    Status.ERROR -> _toastEvent.postEvent(it.error?.localizedDescription)
                }
            }
        }
    }

    fun changeVote(dateId: Long) {
        _itemCurrentlyUpdating.value?.let {
            if (it.second != dateId) _updateItemEvent.postEvent(dateId)
            return
        }
        _itemCurrentlyUpdating.postValue(ItemUpdating(Event.UpdatingType.NONE, dateId))
        Log.d("Logger_DatesVM", "Changing vote with date ID $dateId ...")

        viewModelScope.launch {
            restClient.changeVote(dateId).let {
                _itemCurrentlyUpdating.postValue(null)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { dates -> _dates.postValue(dates) }
                    Status.ERROR -> {
                        _updateItemEvent.postEvent(dateId)
                        _toastEvent.postEvent(it.error?.localizedDescription)
                    }
                }
            }
        }
    }

    private fun removeDateFromList(dateId: Long) {
        _dates.postValue(_dates.value?.filterNot { it.id == dateId })
    }
}
