package com.gkiss01.meetdeb.utils

import android.content.Context
import android.util.Base64
import androidx.core.content.edit

enum class CredentialType {
    EMAIL, PASSWORD
}

fun Context.getAuthToken(default: String = ""): String {
    val sharedPref = this.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    return sharedPref.getString(Constants.PREFERENCES_KEY_AUTH_TOKEN, default) ?: default
}

fun Context.setAuthToken(basic: String? = null) {
    val sharedPref = this.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    sharedPref.edit { putString(Constants.PREFERENCES_KEY_AUTH_TOKEN, basic) }
}

fun Context.getCurrentCredential(type: CredentialType): String {
    val encodedCredentials = getAuthToken().substring("Basic".length).trim()
    val decodedCredentials = String(Base64.decode(encodedCredentials, Base64.DEFAULT))
    val credentials = decodedCredentials.split(":")
    return if (type == CredentialType.EMAIL) credentials[0] else credentials[1]
}
