package com.gkiss01.meetdeb.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.apirequest.EventRequest
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.network.Resource
import com.gkiss01.meetdeb.network.RestClient
import com.squareup.moshi.Moshi
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.threeten.bp.OffsetDateTime
import java.io.File

enum class ScreenType {
    NONE, NEW, UPDATE
}

val createModule = module {
    viewModel { (basic: String) -> EventCreateViewModel(get(), basic, get(), androidApplication()) }
}

class EventCreateViewModel(private val restClient: RestClient, private val basic: String, private val moshi: Moshi, private val application: Application): ViewModel() {
    var eventLocal: Event = Event("", OffsetDateTime.now(), "", "")
    var imageUrl: String = ""
    val type = MutableLiveData<ScreenType>()

    private var _event = MutableLiveData<Resource<Event>>()
    val event: LiveData<Resource<Event>>
        get() = _event

    fun uploadEvent() {
        val file = File(imageUrl)
        var body: MultipartBody.Part? = null

        if (file.exists()) {
            val compressedFile = Compressor(application).compressToFile(file)
            val requestFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        }

        val eventId = if (type.value == ScreenType.NEW) null else eventLocal.id
        val eventRequest = EventRequest(eventId, eventLocal.name, eventLocal.date, eventLocal.venue, eventLocal.description)
        val json = moshi.adapter(EventRequest::class.java).toJson(eventRequest)
        val eventJson: RequestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        if (type.value == ScreenType.NEW) createEvent(eventJson, body)
        else updateEvent(eventJson)
    }

    private fun createEvent(event: RequestBody, image: MultipartBody.Part?) {
        _event.postValue(Resource.loading(null))
        viewModelScope.launch {
            _event.postValue(restClient.createEventAsync(basic, event, image))
        }
    }

    private fun updateEvent(event: RequestBody) {
        _event.postValue(Resource.loading(null))
        viewModelScope.launch {
            _event.postValue(restClient.updateEventAsync(basic, event))
        }
    }

    fun resetLiveData() {
        _event.postValue(Resource.pending(null))
    }

    init {
        type.value = ScreenType.NONE
    }
}
