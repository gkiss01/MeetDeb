package com.gkiss01.meetdeb.data.remote.response

import com.gkiss01.meetdeb.network.common.ErrorCodes

data class SuccessResponse<T> (val withId: T?)
data class ErrorResponse(val errorCode: ErrorCodes, val errors: List<String>?)