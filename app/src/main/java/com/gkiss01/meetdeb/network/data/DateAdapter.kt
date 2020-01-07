package com.gkiss01.meetdeb.network.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class OffsetDateTime

internal class OffsetDateTimeAdapter {
    @ToJson
    fun toJson(@OffsetDateTime date: org.threeten.bp.OffsetDateTime): String {
        return date.toString()
    }

    @FromJson
    @OffsetDateTime
    fun fromJson(date: String): org.threeten.bp.OffsetDateTime {
        return org.threeten.bp.OffsetDateTime.parse(date)
    }
}