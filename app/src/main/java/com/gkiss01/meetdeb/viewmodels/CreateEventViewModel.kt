package com.gkiss01.meetdeb.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.data.apirequest.EventRequest
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.squareup.moshi.Moshi
import id.zelory.compressor.Compressor
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
    viewModel { CreateEventViewModel(get(), androidApplication()) }
}

class CreateEventViewModel(private val moshi: Moshi, private val application: Application): ViewModel() {
    var event: Event = Event("", OffsetDateTime.now(), "", "")
    var imageUrl: String = ""
    val type = MutableLiveData<ScreenType>()

    fun uploadEvent() {
        val file = File(imageUrl)
        var body: MultipartBody.Part? = null

        if (file.exists()) {
            val compressedFile = Compressor(application).compressToFile(file)
            val requestFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        }

        val eventRequest = EventRequest(event.name, event.date, event.venue, event.description)
        val json = moshi.adapter(EventRequest::class.java).toJson(eventRequest)
        val eventJson: RequestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        if (type.value == ScreenType.NEW) MainActivity.instance.createEvent(eventJson, body)
        else MainActivity.instance.updateEvent(event.id, eventJson)
    }

    init {
        type.value = ScreenType.NONE
    }
}
