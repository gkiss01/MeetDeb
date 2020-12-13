package com.gkiss01.meetdeb.data.remote.response

data class EventSummary(
    val userId: Long,
    val eventsCreated: Long,
    val eventsInvolved: Long)