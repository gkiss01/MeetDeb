package com.gkiss01.meetdeb.data

data class User(
    var id: Long,
    var email: String,
    var name: String,
    var enabled: Boolean,
    var roles: Set<Role>)