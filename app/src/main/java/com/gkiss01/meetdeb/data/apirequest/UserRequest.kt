package com.gkiss01.meetdeb.data.apirequest

enum class UserRequestType {
    Create, EmailUpdate, PasswordUpdate
}

data class UserRequest(
    val email: String,
    val password: String,
    val name: String,
    val type: Int)