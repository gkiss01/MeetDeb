package com.gkiss01.meetdeb.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.data.request.EventRequest
import com.gkiss01.meetdeb.network.moshi
import id.zelory.compressor.Compressor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import java.io.File

class CreateEventViewModel(application: Application): AndroidViewModel(application) {
    var year: Int
    var month: Int
    var day: Int
    var hour: Int
    var minute: Int
    var zoneOffset: ZoneOffset

    var eventName: String = ""
    var eventVenue: String = ""
    var eventDescription: String = ""

    val imageUrl = MutableLiveData<String>()
    private val _dateTime = MutableLiveData<OffsetDateTime>()
    val dateTime: LiveData<OffsetDateTime>
    get() = _dateTime

    fun calculateDateTime() {
        _dateTime.value = OffsetDateTime.of(year, month, day, hour, minute, 0, 0, zoneOffset)
    }

    fun createEvent() {
        val file = File(imageUrl.value!!)
        var body: MultipartBody.Part? = null

        if (file.exists()) {
            val compressedFile = Compressor(getApplication()).compressToFile(file)
            val requestFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        }

        val eventRequest = EventRequest(eventName, dateTime.value!!, eventVenue, eventDescription)
        val json = moshi.adapter(EventRequest::class.java).toJson(eventRequest)
        val event: RequestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        MainActivity.instance.uploadEvent(event, body)
    }

    init {
        val date: OffsetDateTime = OffsetDateTime.now()

        year = date.year
        month = date.monthValue
        day = date.dayOfMonth
        hour = date.hour
        minute = date.minute
        zoneOffset = date.offset

        _dateTime.value = date
        imageUrl.value = ""
    }
}
