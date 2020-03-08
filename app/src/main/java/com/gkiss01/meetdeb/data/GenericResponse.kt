package com.gkiss01.meetdeb.data

import com.gkiss01.meetdeb.data.fastadapter.Date
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.data.fastadapter.Participant
import com.gkiss01.meetdeb.network.ErrorCodes

data class GenericResponse(
    val error: Boolean,
    val errorCode: ErrorCodes?,
    val errors: List<String>?,
    val withId: Long?,

    val user: User?,
    val event: Event?,
    val events: List<Event>?,
    val dates: List<Date>?,
    val participants: List<Participant>?
)