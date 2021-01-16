package com.gkiss01.meetdeb.data.remote.response

import com.gkiss01.meetdeb.network.common.Resource.ErrorCode

data class SuccessResponse<T> (val withId: T?)
data class ErrorResponse(val errorCode: ErrorCode, val errors: List<String>?)