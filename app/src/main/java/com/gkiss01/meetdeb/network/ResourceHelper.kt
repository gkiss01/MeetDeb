package com.gkiss01.meetdeb.network

import retrofit2.HttpException
import java.net.SocketTimeoutException

enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    PENDING
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