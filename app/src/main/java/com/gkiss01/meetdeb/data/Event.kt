package com.gkiss01.meetdeb.data

import org.threeten.bp.OffsetDateTime

data class Event(
    var id: Long,

    var username: String,

    var name: String,

    @com.gkiss01.meetdeb.adapter.OffsetDateTime
    var date: OffsetDateTime,

    var venue: String,

    var labels: String,

    var participants: Int,

    val accepted: Boolean,

    val voted: Boolean)