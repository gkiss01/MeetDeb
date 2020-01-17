package com.gkiss01.meetdeb.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.request.EventRequest
import com.gkiss01.meetdeb.network.WebApi
import com.gkiss01.meetdeb.network.moshi
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.io.File

class CreateEventViewModel(application: Application): AndroidViewModel(application) {

    private val basic = Credentials.basic("gergokiss04@gmail.com", "asdasdasd")
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy, HH:mm")

    var year: Int
    var month: Int
    var day: Int
    var hour: Int
    var minute: Int
    var zoneOffset: ZoneOffset

    var eventName: String = ""
    var eventVenue: String = ""
    var eventLabels: String = ""

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
            val requestFile = RequestBody.create(MediaType.parse("image/*"), compressedFile)
            body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        }

        val eventRequest = EventRequest(dateTime.value!!, eventVenue, eventLabels)
        val json = moshi.adapter(EventRequest::class.java).toJson(eventRequest)
        val event: RequestBody = RequestBody.create(MediaType.parse("application/json"), json)

        uploadEvent(event, body)
    }

    private fun uploadEvent(event: RequestBody, image: MultipartBody.Part?) {
        viewModelScope.launch {
            val createEventDeferred = WebApi.retrofitService.createEventAsync(basic, event, image)
            try {
                val listResult = createEventDeferred.await()
                Log.d("CreateEvent", listResult.toString())
            }
            catch (e: Exception) {
                Log.d("CreateEvent", "error: $e")
            }
        }
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
