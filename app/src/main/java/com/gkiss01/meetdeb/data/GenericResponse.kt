package com.gkiss01.meetdeb.data

data class GenericResponse(
    val error: Boolean,

    val errors: List<String>?,

    val events: List<Event>?
)