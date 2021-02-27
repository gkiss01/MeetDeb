package com.gkiss01.meetdeb.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.remote.request.EventRequest
import com.gkiss01.meetdeb.data.remote.response.Event
import com.gkiss01.meetdeb.network.api.RestClient
import com.gkiss01.meetdeb.network.common.Resource.Status
import com.gkiss01.meetdeb.utils.SingleEvent
import com.gkiss01.meetdeb.utils.VoidEvent
import com.gkiss01.meetdeb.utils.postEvent
import com.squareup.moshi.Moshi
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

enum class ScreenType {
    NEW, UPDATE
}

class EventCreateViewModel(private val restClient: RestClient, private val moshi: Moshi, private val application: Application): ViewModel() {
    lateinit var type: ScreenType
    lateinit var eventLocal: Event
    fun isEventInitialized() = ::eventLocal.isInitialized

    private val _toastEvent = MutableLiveData<SingleEvent<Any>>()
    val toastEvent: LiveData<SingleEvent<Any>>
        get() = _toastEvent

    private val _itemCurrentlyAdding = MutableLiveData<Boolean>()
    val itemCurrentlyAdding: LiveData<Boolean>
        get() = _itemCurrentlyAdding

    private val _operationSuccessful = MutableLiveData<VoidEvent>()
    val operationSuccessful: LiveData<VoidEvent>
        get() = _operationSuccessful

    val pickedImageUri = MutableLiveData<String>()

    fun uploadEvent() {
        if (_itemCurrentlyAdding.value == true) return
        val event = prepareEvent()
        if (type == ScreenType.NEW) createEvent(event, prepareImage())
        else updateEvent(event)
    }

    private fun createEvent(event: RequestBody, image: MultipartBody.Part?) {
        if (_itemCurrentlyAdding.value == true) return
        _itemCurrentlyAdding.postValue(true)
        Log.d("Logger_EventCreateVM", "Creating event ...")

        viewModelScope.launch {
            restClient.createEvent(event, image).let {
                _itemCurrentlyAdding.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { _operationSuccessful.postEvent() }
                    Status.ERROR -> _toastEvent.postEvent(it.error?.localizedDescription)
                }
            }
        }
    }

    private fun updateEvent(event: RequestBody) {
        if (_itemCurrentlyAdding.value == true) return
        _itemCurrentlyAdding.postValue(true)
        Log.d("Logger_EventCreateVM", "Updating event ...")

        viewModelScope.launch {
            restClient.updateEvent(event).let {
                _itemCurrentlyAdding.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { _operationSuccessful.postEvent() }
                    Status.ERROR -> _toastEvent.postEvent(it.error?.localizedDescription)
                }
            }
        }
    }

    private fun prepareEvent(): RequestBody {
        val eventId = if (type == ScreenType.NEW) null else eventLocal.id
        val eventRequest = EventRequest(eventId, eventLocal.name, eventLocal.date, eventLocal.venue, eventLocal.description)
        val json = moshi.adapter(EventRequest::class.java).toJson(eventRequest)
        return json.toRequestBody("application/json".toMediaTypeOrNull())
    }

    private fun prepareImage(): MultipartBody.Part? {
        val imageUri = pickedImageUri.value ?: return null
        val file = File(imageUri)
        if (!file.exists()) return null

        val compressedFile = Compressor(application).compressToFile(file)
        val requestFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", file.name, requestFile)
    }

    init {
        _itemCurrentlyAdding.value = false
    }
}
