package com.gkiss01.meetdeb.network

import com.gkiss01.meetdeb.data.ErrorResponse
import com.squareup.moshi.Moshi
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    PENDING
}

enum class ErrorCodes {
    USER_DISABLED_OR_NOT_VALID,
    ACCESS_DENIED,
    BAD_REQUEST_FORMAT,
    TIMEOUT,
    CONNECT,
    UNKNOWN,

    USER_NOT_FOUND,
    NO_USERS_FOUND,
    CONFIRMATION_TOKEN_NOT_FOUND,
    USER_ALREADY_VERIFIED,
    EMAIL_ALREADY_IN_USE,

    EVENT_NOT_FOUND,
    NO_EVENTS_FOUND,

    PARTICIPANT_NOT_FOUND,
    PARTICIPANT_ALREADY_CREATED,

    DATE_NOT_FOUND,
    NO_DATES_FOUND,
    DATE_ALREADY_CREATED,

    VOTE_NOT_FOUND,
    VOTE_ALREADY_CREATED,

    FILE_NOT_FOUND,
    FILENAME_INVALID,
    COULD_NOT_CONVERT_IMAGE,
    FILE_SIZE_LIMIT_EXCEEDED,
    UPLOAD_FAILED,
    COULD_NOT_CREATE_DIRECTORY;
}

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

open class ResourceHandler(private val moshi: Moshi) {
    fun <T : Any> handleSuccess(data: T): Resource<T> {
        return Resource.success(data)
    }

    fun <T : Any> handleException(e: Exception): Resource<T> {
        return when (e) {
            is HttpException -> {
                val errorResponse = convertErrorBody(e)
                Resource.error(convertErrorMessage(errorResponse, e.code() >= 500), null)
            }
            is SocketTimeoutException -> Resource.error(getErrorString(ErrorCodes.TIMEOUT), null)
            is ConnectException -> Resource.error(getErrorString(ErrorCodes.CONNECT), null)
            else -> Resource.error(getErrorString(null), null)
        }
    }

    private fun convertErrorBody(throwable: HttpException): ErrorResponse? {
        return try {
            throwable.response()?.errorBody()?.source()?.let {
                moshi.adapter(ErrorResponse::class.java).fromJson(it)
            }
        } catch (exception: Exception) {
            null
        }
    }

    private fun convertErrorMessage(errorResponse: ErrorResponse?, isServerError: Boolean = false): String {
        var errorMessage = getErrorString(errorResponse?.errorCode)

        errorResponse?.errors?.let {
            errorMessage = ""
            it.forEachIndexed { index, e  ->
                errorMessage = errorMessage.plus(if (index == 0) "" else "\n").plus(e)
            }
        }

        if (isServerError)
            errorMessage = "[Server error]\n".plus(errorMessage)
        return errorMessage
    }

    private fun getErrorString(errorCode: ErrorCodes?): String {
        return when (errorCode) {
            ErrorCodes.USER_NOT_FOUND -> "User is not found!"
            ErrorCodes.NO_USERS_FOUND -> "No users found!"
            ErrorCodes.CONFIRMATION_TOKEN_NOT_FOUND -> "Confirmation token is not found!"
            ErrorCodes.EMAIL_ALREADY_IN_USE -> "Email is already in use!"
            ErrorCodes.USER_ALREADY_VERIFIED -> "User is already verified!"
            ErrorCodes.EVENT_NOT_FOUND -> "Event is not found!"
            ErrorCodes.NO_EVENTS_FOUND -> "No events found!"
            ErrorCodes.PARTICIPANT_NOT_FOUND -> "Participant is not found!"
            ErrorCodes.PARTICIPANT_ALREADY_CREATED -> "Participant is already created!"
            ErrorCodes.DATE_NOT_FOUND -> "Date is not found!"
            ErrorCodes.NO_DATES_FOUND -> "No dates found!"
            ErrorCodes.DATE_ALREADY_CREATED -> "Date is already created!"
            ErrorCodes.VOTE_NOT_FOUND -> "Vote is not found!"
            ErrorCodes.VOTE_ALREADY_CREATED -> "Vote is already created!"
            ErrorCodes.FILE_NOT_FOUND -> "File is not found!"
            ErrorCodes.FILENAME_INVALID -> "Filename is invalid!"
            ErrorCodes.COULD_NOT_CONVERT_IMAGE -> "Could not convert image!"
            ErrorCodes.FILE_SIZE_LIMIT_EXCEEDED -> "Size limit is exceeded!"
            ErrorCodes.UPLOAD_FAILED -> "Upload failed!"
            ErrorCodes.COULD_NOT_CREATE_DIRECTORY -> "Could not create directory!"
            ErrorCodes.BAD_REQUEST_FORMAT -> "Bad request format!"
            ErrorCodes.ACCESS_DENIED -> "Access is denied!"
            ErrorCodes.USER_DISABLED_OR_NOT_VALID -> "User is disabled or not valid!"
            ErrorCodes.TIMEOUT -> "Server timeout!"
            ErrorCodes.CONNECT -> "Connection error!"
            else -> "Something went wrong!"
        }
    }
}