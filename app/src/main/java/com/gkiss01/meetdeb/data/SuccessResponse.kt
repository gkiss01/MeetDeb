package com.gkiss01.meetdeb.data

import com.gkiss01.meetdeb.network.ErrorCodes

data class SuccessResponse<T> (val withId: T?)
data class ErrorResponse(
    val errorCode: ErrorCodes,
    val errors: List<String>?)