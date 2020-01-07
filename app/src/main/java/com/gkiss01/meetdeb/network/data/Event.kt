package com.gkiss01.meetdeb.network.data

import org.threeten.bp.OffsetDateTime

data class Event(
    var id: Long,

    var username: String,

    @com.gkiss01.meetdeb.network.data.OffsetDateTime
    var date: OffsetDateTime,

    var venue: String,

    var labels: String)