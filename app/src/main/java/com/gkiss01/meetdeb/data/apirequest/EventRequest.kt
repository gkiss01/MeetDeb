package com.gkiss01.meetdeb.data.apirequest

import org.threeten.bp.OffsetDateTime

class EventRequest(
    val name: String,
    @com.gkiss01.meetdeb.adapter.OffsetDateTime
    val date: OffsetDateTime,
    val venue: String,
    val description: String
)