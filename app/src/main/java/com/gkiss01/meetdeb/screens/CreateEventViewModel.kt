package com.gkiss01.meetdeb.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.data.apirequest.EventRequest
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.network.moshi
import id.zelory.compressor.Compressor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.threeten.bp.OffsetDateTime
import java.io.File

class CreateEventViewModel(application: Application): AndroidViewModel(application) {
    lateinit var event: Event

    var eventName: String = ""
    var eventVenue: String = ""
    var eventDescription: String = ""
    var eventDate: OffsetDateTime = OffsetDateTime.now()

    val imageUrl = MutableLiveData<String>()

    fun createEvent() {
        val file = File(imageUrl.value!!)
        var body: MultipartBody.Part? = null

        if (file.exists()) {
            val compressedFile = Compressor(getApplication()).compressToFile(file)
            val requestFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        }

        val eventRequest = EventRequest(eventName, eventDate, eventVenue, eventDescription)
        val json = moshi.adapter(EventRequest::class.java).toJson(eventRequest)
        val event: RequestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        MainActivity.instance.uploadEvent(event, body)
    }

    init {
        imageUrl.value = ""
    }
}
