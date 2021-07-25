package com.gkiss01.meetdeb.utils

import android.content.Context
import android.util.Base64
import androidx.core.content.edit

enum class CredentialType {
    EMAIL, PASSWORD
}

fun Context.getAuthToken(default: String = ""): String {
    val sharedPref = this.getSharedPreferences("BASIC_AUTH_PREFS", Context.MODE_PRIVATE)
    return sharedPref.getString("AUTH_TOKEN_BASIC", default) ?: default
}

fun Context.setAuthToken(basic: String? = null) {
    val sharedPref = this.getSharedPreferences("BASIC_AUTH_PREFS", Context.MODE_PRIVATE)
    sharedPref.edit { putString("AUTH_TOKEN_BASIC", basic) }
}

fun Context.getCurrentCredential(type: CredentialType): String {
    val encodedCredentials = getAuthToken().substring("Basic".length).trim()
    val decodedCredentials = String(Base64.decode(encodedCredentials, Base64.DEFAULT))
    val credentials = decodedCredentials.split(":")
    return if (type == CredentialType.EMAIL) credentials[0] else credentials[1]
}
