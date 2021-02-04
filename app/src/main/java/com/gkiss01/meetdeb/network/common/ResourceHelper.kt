package com.gkiss01.meetdeb.network.common

import android.app.Application
import android.content.Context
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.remote.response.ErrorResponse
import com.gkiss01.meetdeb.network.common.Error.ErrorCode
import com.squareup.moshi.Moshi
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

data class Error(val code: ErrorCode, val localizedDescription: String?) {

    enum class ErrorCode {
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

        fun getDescription(context: Context): String {
            return when (this) {
                USER_NOT_FOUND -> context.getString(R.string.user_not_found)
                NO_USERS_FOUND -> context.getString(R.string.no_users_found)
                CONFIRMATION_TOKEN_NOT_FOUND -> context.getString(R.string.confirmation_token_not_found)
                EMAIL_ALREADY_IN_USE -> context.getString(R.string.email_already_in_use)
                USER_ALREADY_VERIFIED -> context.getString(R.string.user_already_verified)
                EVENT_NOT_FOUND -> context.getString(R.string.event_not_found)
                NO_EVENTS_FOUND -> context.getString(R.string.no_events_found)
                PARTICIPANT_NOT_FOUND -> context.getString(R.string.participant_not_found)
                DATE_NOT_FOUND -> context.getString(R.string.date_not_found)
                NO_DATES_FOUND -> context.getString(R.string.no_dates_found)
                DATE_ALREADY_CREATED -> context.getString(R.string.date_already_created)
                FILE_NOT_FOUND -> context.getString(R.string.file_not_found)
                FILENAME_INVALID -> context.getString(R.string.filename_invalid)
                COULD_NOT_CONVERT_IMAGE -> context.getString(R.string.could_not_convert_image)
                FILE_SIZE_LIMIT_EXCEEDED -> context.getString(R.string.file_size_limit_exceeded)
                UPLOAD_FAILED -> context.getString(R.string.upload_failed)
                COULD_NOT_CREATE_DIRECTORY -> context.getString(R.string.could_not_create_directory)
                BAD_REQUEST_FORMAT -> context.getString(R.string.bad_request_format)
                ACCESS_DENIED -> context.getString(R.string.access_denied)
                USER_DISABLED_OR_NOT_VALID -> context.getString(R.string.user_disabled_or_not_valid)
                TIMEOUT -> context.getString(R.string.timeout_error)
                CONNECT -> context.getString(R.string.connect_error)
                else -> context.getString(R.string.unknown_error)
            }
        }
    }
}

data class Resource<out T>(val status: Status, val data: T?, val error: Error?) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(error: Error?, data: T? = null): Resource<T> {
            return Resource(Status.ERROR, data, error)
        }
    }

    enum class Status {
        SUCCESS,
        ERROR
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
                val error = errorResponse?.asError(application, e.code() >= 500)
                Resource.error(error, null)
            }
            is SocketTimeoutException -> Resource.error(Error(ErrorCode.TIMEOUT, ErrorCode.TIMEOUT.getDescription(application)))
            is ConnectException -> Resource.error(Error(ErrorCode.CONNECT, ErrorCode.CONNECT.getDescription(application)))
            else -> Resource.error(Error(ErrorCode.UNKNOWN, ErrorCode.UNKNOWN.getDescription(application)))
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
}