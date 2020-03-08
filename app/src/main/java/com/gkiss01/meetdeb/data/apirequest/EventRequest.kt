package com.gkiss01.meetdeb.data.apirequest

import org.threeten.bp.OffsetDateTime

class EventRequest(
    private val name: String,

    @com.gkiss01.meetdeb.adapter.OffsetDateTime
    private val date: OffsetDateTime,

    private val venue: String,

    private val description: String
)