package com.gkiss01.meetdeb.utils

import android.content.Context
import android.util.Base64
import androidx.core.content.edit

interface AuthManager {
    fun getAuthToken(): String
    fun setAuthToken(basic: String?)
    fun getCredential(type: CredentialType): String?

    enum class CredentialType {
        EMAIL, PASSWORD
    }
}

class AuthManagerImpl(context: Context, private val defaultToken: String = ""): AuthManager {
    private val sharedPref = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

    override fun getAuthToken() = sharedPref.getString(Constants.PREFERENCES_KEY_AUTH_TOKEN, defaultToken) ?: defaultToken

    override fun setAuthToken(basic: String?) {
        sharedPref.edit { putString(Constants.PREFERENCES_KEY_AUTH_TOKEN, basic) }
    }

    override fun getCredential(type: AuthManager.CredentialType): String? {
        val encodedCredentials = getAuthToken().substring("Basic".length).trim()
        val decodedCredentials = String(Base64.decode(encodedCredentials, Base64.DEFAULT))
        val credentials = decodedCredentials.split(":")
        return credentials.getOrNull(if (type == AuthManager.CredentialType.EMAIL) 0 else 1)
    }
}