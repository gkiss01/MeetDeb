package com.gkiss01.meetdeb.utils

import android.content.Context
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.data.isAdmin
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy, HH:mm")

fun isActiveUserAdmin() = MainActivity.instance.getActiveUser()!!.isAdmin()

fun formatDate(offsetDateTime: OffsetDateTime): String = offsetDateTime.format(dateFormatter)

fun updateOffsetDateTime(offsetDateTime: OffsetDateTime, year: Int, monthValue: Int, dayOfMonth: Int): OffsetDateTime {
    return OffsetDateTime.of(year, monthValue, dayOfMonth, offsetDateTime.hour, offsetDateTime.minute,
        offsetDateTime.second, offsetDateTime.nano, offsetDateTime.offset)
}

fun updateOffsetDateTime(offsetDateTime: OffsetDateTime, hourOfDay: Int, minute: Int): OffsetDateTime {
    return OffsetDateTime.of(offsetDateTime.year, offsetDateTime.monthValue, offsetDateTime.dayOfMonth, hourOfDay, minute,
        offsetDateTime.second, offsetDateTime.nano, offsetDateTime.offset)
}

fun isDate24HourFormat(context: Context) = android.text.format.DateFormat.is24HourFormat(context)