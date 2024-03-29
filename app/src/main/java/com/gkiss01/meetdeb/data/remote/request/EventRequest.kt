package com.gkiss01.meetdeb.data.remote.request

import com.gkiss01.meetdeb.utils.OffsetDateTimeCustom
import org.threeten.bp.OffsetDateTime

data class EventRequest(
    val id: Long?,
    val name: String?,
    @OffsetDateTimeCustom
    val date: OffsetDateTime?,
    val venue: String?,
    val description: String?)