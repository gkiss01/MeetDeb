package com.gkiss01.meetdeb.network.data

data class GenericResponse(
    val error: Boolean,

    val errors: List<String>?,

    val events: List<Event>?
)