package com.gkiss01.meetdeb.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter

class CreateEventViewModel(application: Application): AndroidViewModel(application) {
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

    private val _dateTime = MutableLiveData<OffsetDateTime>()
    val dateTime: LiveData<OffsetDateTime>
    get() = _dateTime

    fun calculateDateTime() {
        _dateTime.value = OffsetDateTime.of(year, month, day, hour, minute, 0, 0, zoneOffset)
    }

    fun createEvent() {

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
    }
}
