package com.gkiss01.meetdeb.data.remote.response

import android.content.Context
import com.gkiss01.meetdeb.network.common.Error
import com.gkiss01.meetdeb.network.common.Error.ErrorCode

data class SuccessResponse<T>(val withId: T?)
data class ErrorResponse(val errorCode: ErrorCode, val errors: List<String>?) {
    fun asError(context: Context, isServerError: Boolean = false): Error {
        var errorMessage = errorCode.getDescription(context)

        errors?.let {
            errorMessage = ""
            it.forEachIndexed { index, e  ->
                errorMessage = errorMessage.plus(if (index == 0) "" else "\n").plus(e)
            }
        }

        if (isServerError)
            errorMessage = "[Server error]\n".plus(errorMessage)
        return Error(errorCode, errorMessage)
    }
}