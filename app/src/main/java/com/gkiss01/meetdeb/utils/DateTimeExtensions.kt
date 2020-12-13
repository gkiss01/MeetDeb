package com.gkiss01.meetdeb.utils

import android.content.Context
import android.text.format.DateFormat
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

fun OffsetDateTime.update(year: Int, monthValue: Int, dayOfMonth: Int): OffsetDateTime = OffsetDateTime.of(year, monthValue, dayOfMonth, this.hour, this.minute,
    this.second, this.nano, this.offset)

fun OffsetDateTime.update(hourOfDay: Int, minute: Int): OffsetDateTime = OffsetDateTime.of(this.year, this.monthValue, this.dayOfMonth, hourOfDay, minute,
    this.second, this.nano, this.offset)

fun OffsetDateTime.format(): String = this.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy, HH:mm"))

fun Context.isTimeIn24HourFormat() = DateFormat.is24HourFormat(this)