package com.gkiss01.meetdeb.data

enum class Role {
    ROLE_CLIENT, ROLE_ADMIN
}

data class User(
    val id: Long,
    val email: String,
    val name: String,
    val enabled: Boolean,
    val roles: Set<Role>)

fun User.isAdmin() = this.roles.contains(Role.ROLE_ADMIN)