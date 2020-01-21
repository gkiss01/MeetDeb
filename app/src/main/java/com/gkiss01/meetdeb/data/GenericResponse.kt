package com.gkiss01.meetdeb.data

data class GenericResponse(
    val error: Boolean,

    val errors: List<String>?,

    val event: Event?,

    val events: List<Event>?,

    val dates: List<Date>?
)