package com.gkiss01.meetdeb.utils.classes

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import org.threeten.bp.OffsetDateTime

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class OffsetDateTimeCustom

internal class OffsetDateTimeAdapter {
    @ToJson
    fun toJson(@OffsetDateTimeCustom date: OffsetDateTime): String {
        return date.toString()
    }

    @FromJson
    @OffsetDateTimeCustom
    fun fromJson(date: String): OffsetDateTime {
        return OffsetDateTime.parse(date)
    }
}