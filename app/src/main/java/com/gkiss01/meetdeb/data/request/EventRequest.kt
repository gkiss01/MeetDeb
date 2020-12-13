package com.gkiss01.meetdeb.data.request

import com.gkiss01.meetdeb.utils.classes.OffsetDateTimeCustom
import org.threeten.bp.OffsetDateTime

data class EventRequest(
    val id: Long?,
    val name: String?,
    @OffsetDateTimeCustom
    val date: OffsetDateTime?,
    val venue: String?,
    val description: String?)