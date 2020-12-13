package com.gkiss01.meetdeb.data.remote.response

data class User(
    val id: Long,
    val email: String,
    val name: String,
    val enabled: Boolean,
    val roles: Set<Role>) {

    enum class Role {
        ROLE_CLIENT, ROLE_ADMIN
    }
}

fun User.isAdmin() = this.roles.contains(User.Role.ROLE_ADMIN)