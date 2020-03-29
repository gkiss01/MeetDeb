package com.gkiss01.meetdeb.data.apirequest

enum class UserRequestType {
    Create, EmailUpdate, PasswordUpdate
}

data class UserRequest(
    private val email: String,
    private val password: String,
    private val name: String,
    private val type: Int)