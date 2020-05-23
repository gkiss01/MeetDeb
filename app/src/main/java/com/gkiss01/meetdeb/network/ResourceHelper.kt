package com.gkiss01.meetdeb.network

import retrofit2.HttpException
import java.net.SocketTimeoutException

enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    PENDING
}

//enum class ErrorCodes {
//    USER_DISABLED_OR_NOT_VALID,
//    ACCESS_DENIED,
//    BAD_REQUEST_FORMAT,
//    UNKNOWN,
//
//    USER_NOT_FOUND,
//    NO_USERS_FOUND,
//    CONFIRMATION_TOKEN_NOT_FOUND,
//    USER_ALREADY_VERIFIED,
//    EMAIL_ALREADY_IN_USE,
//
//    EVENT_NOT_FOUND,
//    NO_EVENTS_FOUND,
//    PARTICIPANT_NOT_FOUND,
//    NO_PARTICIPANTS_FOUND,
//
//    PARTICIPANT_ALREADY_CREATED,
//    DATE_NOT_FOUND,
//    NO_DATES_FOUND,
//    DATE_ALREADY_CREATED,
//
//    VOTE_NOT_FOUND,
//    VOTE_ALREADY_CREATED,
//
//    FILE_NOT_FOUND,
//    FILENAME_INVALID,
//    COULD_NOT_CONVERT_IMAGE,
//    FILE_SIZE_LIMIT_EXCEEDED,
//    UPLOAD_FAILED,
//    COULD_NOT_CREATE_DIRECTORY;
//}

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }

        fun <T> pending(data: T?): Resource<T> {
            return Resource(Status.PENDING, data, null)
        }
    }
}

open class ResourceHandler {
    fun <T : Any> handleSuccess(data: T): Resource<T> {
        return Resource.success(data)
    }

    fun <T : Any> handleException(e: Exception): Resource<T> {
        return when (e) {
            is HttpException -> Resource.error(getErrorMessage(e.code()), null)
            is SocketTimeoutException -> Resource.error(getErrorMessage(408), null)
            else -> Resource.error(getErrorMessage(Int.MAX_VALUE), null)
        }
    }

    private fun getErrorMessage(code: Int): String {
        return when (code) {
            401 -> "Unauthorised"
            404 -> "Not found"
            408 -> "Timeout"
            else -> "Something went wrong"
        }
    }
}

//    private fun handleErrors(e: Exception) {
//        val errors = when (e) {
//            is SocketTimeoutException -> "Connection error! (server)"
//            is ConnectException -> "Connection error! (client)"
//            else -> e.message
//        }
//        Log.d("MainActivityApiCall", "Failure: $errors")
//        Log.d("MainActivityApiCall", "$e")
//        Toast.makeText(this, errors, Toast.LENGTH_LONG).show()
//    }
//
//    private fun handleResponseErrors(errorCode: ErrorCodes, errors: List<String>) {
//        var errorsMsg = ""
//        Log.d("MainActivityApiCall", "Failure: ${errors.size} errors:")
//        errors.forEachIndexed { index, e  ->
//            run {
//                Log.d("MainActivityApiCall", e)
//                errorsMsg = errorsMsg.plus(if (index == 0) "" else "\n").plus(e)
//            }
//        }
//        Toast.makeText(this, errorsMsg, Toast.LENGTH_LONG).show()
//    }