package com.gkiss01.meetdeb.data

import com.gkiss01.meetdeb.network.ErrorCodes

data class GenericResponse(
    val error: Boolean,

    val errorCode: ErrorCodes?,

    val errors: List<String>?,

    val user: User?,

    val event: Event?,

    val events: List<Event>?,

    val dates: List<Date>?
)