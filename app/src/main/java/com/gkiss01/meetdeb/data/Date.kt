package com.gkiss01.meetdeb.data

import org.threeten.bp.OffsetDateTime

data class Date(
    var id: Long,

    var eventId: Long,

    @com.gkiss01.meetdeb.adapter.OffsetDateTime
    var date: OffsetDateTime)