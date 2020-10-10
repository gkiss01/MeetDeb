package com.gkiss01.meetdeb.data.request

data class UserRequest(
    var email: String?,
    var password: String?,
    var name: String?) {

    constructor(): this(null, null, null)
}