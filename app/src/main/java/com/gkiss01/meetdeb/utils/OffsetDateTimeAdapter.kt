package com.gkiss01.meetdeb.utils

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import org.threeten.bp.OffsetDateTime

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class OffsetDateTimeCustom

class OffsetDateTimeAdapter {
    @ToJson
    fun toJson(@OffsetDateTimeCustom date: OffsetDateTime): String = date.toString()

    @FromJson
    @OffsetDateTimeCustom
    fun fromJson(date: String): OffsetDateTime = OffsetDateTime.parse(date)
}