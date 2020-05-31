package com.gkiss01.meetdeb.network

import android.app.Application
import com.gkiss01.meetdeb.R
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

    DATE_NOT_FOUND,
    NO_DATES_FOUND,
    DATE_ALREADY_CREATED,

    FILE_NOT_FOUND,
    FILENAME_INVALID,
    COULD_NOT_CONVERT_IMAGE,
    FILE_SIZE_LIMIT_EXCEEDED,
    UPLOAD_FAILED,
    COULD_NOT_CREATE_DIRECTORY;
}

data class Resource<out T>(val status: Status, val data: T?, val errorCode: ErrorCodes?, val errorMessage: String?) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null, null)
        }

        fun <T> error(code: ErrorCodes, msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, code, msg)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null, null)
        }

        fun <T> pending(data: T?): Resource<T> {
            return Resource(Status.PENDING, data, null, null)
        }
    }
}

open class ResourceHandler(private val moshi: Moshi, private val application: Application) {
    fun <T : Any> handleSuccess(data: T): Resource<T> {
        return Resource.success(data)
    }

    fun <T : Any> handleException(e: Exception): Resource<T> {
        return when (e) {
            is HttpException -> {
                val errorResponse = convertErrorBody(e)
                Resource.error(errorResponse?.errorCode ?: ErrorCodes.UNKNOWN, convertErrorMessage(errorResponse, e.code() >= 500), null)
            }
            is SocketTimeoutException -> Resource.error(ErrorCodes.TIMEOUT, getErrorString(ErrorCodes.TIMEOUT), null)
            is ConnectException -> Resource.error(ErrorCodes.CONNECT, getErrorString(ErrorCodes.CONNECT), null)
            else -> Resource.error(ErrorCodes.UNKNOWN, getErrorString(null), null)
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
            ErrorCodes.USER_NOT_FOUND -> application.getString(R.string.user_not_found)
            ErrorCodes.NO_USERS_FOUND -> application.getString(R.string.no_users_found)
            ErrorCodes.CONFIRMATION_TOKEN_NOT_FOUND -> application.getString(R.string.confirmation_token_not_found)
            ErrorCodes.EMAIL_ALREADY_IN_USE -> application.getString(R.string.email_already_in_use)
            ErrorCodes.USER_ALREADY_VERIFIED -> application.getString(R.string.user_already_verified)
            ErrorCodes.EVENT_NOT_FOUND -> application.getString(R.string.event_not_found)
            ErrorCodes.NO_EVENTS_FOUND -> application.getString(R.string.no_events_found)
            ErrorCodes.PARTICIPANT_NOT_FOUND -> application.getString(R.string.participant_not_found)
            ErrorCodes.DATE_NOT_FOUND -> application.getString(R.string.date_not_found)
            ErrorCodes.NO_DATES_FOUND -> application.getString(R.string.no_dates_found)
            ErrorCodes.DATE_ALREADY_CREATED -> application.getString(R.string.date_already_created)
            ErrorCodes.FILE_NOT_FOUND -> application.getString(R.string.file_not_found)
            ErrorCodes.FILENAME_INVALID -> application.getString(R.string.filename_invalid)
            ErrorCodes.COULD_NOT_CONVERT_IMAGE -> application.getString(R.string.could_not_convert_image)
            ErrorCodes.FILE_SIZE_LIMIT_EXCEEDED -> application.getString(R.string.file_size_limit_exceeded)
            ErrorCodes.UPLOAD_FAILED -> application.getString(R.string.upload_failed)
            ErrorCodes.COULD_NOT_CREATE_DIRECTORY -> application.getString(R.string.could_not_create_directory)
            ErrorCodes.BAD_REQUEST_FORMAT -> application.getString(R.string.bad_request_format)
            ErrorCodes.ACCESS_DENIED -> application.getString(R.string.access_denied)
            ErrorCodes.USER_DISABLED_OR_NOT_VALID -> application.getString(R.string.user_disabled_or_not_valid)
            ErrorCodes.TIMEOUT -> application.getString(R.string.timeout_error)
            ErrorCodes.CONNECT -> application.getString(R.string.connect_error)
            else -> application.getString(R.string.unknown_error)
        }
    }
}