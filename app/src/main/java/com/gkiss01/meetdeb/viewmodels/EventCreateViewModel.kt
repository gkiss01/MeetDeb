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
    ADD, UPDATE
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
        val eventId = if (type == ScreenType.ADD) null else eventLocal.id
        val eventRequest = EventRequest(eventId, eventLocal.name, eventLocal.date, eventLocal.venue, eventLocal.description)
        val json = moshi.adapter(EventRequest::class.java).toJson(eventRequest)
        val eventJson: RequestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        if (type == ScreenType.ADD) {
            var body: MultipartBody.Part? = null
            pickedImageUri.value?.let { uri ->
                val file = File(uri)
                if (file.exists()) {
                    val compressedFile = Compressor(application).compressToFile(file)
                    val requestFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                }
            }
            createEvent(eventJson, body)
        }
        else updateEvent(eventJson)
    }

    private fun createEvent(event: RequestBody, image: MultipartBody.Part?) {
        if (_itemCurrentlyAdding.value == true) return
        Log.d("Logger_EventCreateVM", "Creating event ...")
        _itemCurrentlyAdding.postValue(true)
        viewModelScope.launch {
            restClient.createEvent(event, image).let {
                _itemCurrentlyAdding.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { _operationSuccessful.postEvent() }
                    Status.ERROR -> _toastEvent.postEvent(it.errorMessage)
                }
            }
        }
    }

    private fun updateEvent(event: RequestBody) {
        if (_itemCurrentlyAdding.value == true) return
        Log.d("Logger_EventCreateVM", "Updating event ...")
        _itemCurrentlyAdding.postValue(true)
        viewModelScope.launch {
            restClient.updateEvent(event).let {
                _itemCurrentlyAdding.postValue(false)
                when (it.status) {
                    Status.SUCCESS -> it.data?.let { _operationSuccessful.postEvent() }
                    Status.ERROR -> _toastEvent.postEvent(it.errorMessage)
                }
            }
        }
    }

    init {
        _itemCurrentlyAdding.value = false
    }
}
