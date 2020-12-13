package com.gkiss01.meetdeb

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.data.remote.response.User
import com.gkiss01.meetdeb.network.common.Resource
import com.gkiss01.meetdeb.utils.CredentialType
import com.gkiss01.meetdeb.utils.getCurrentCredential
import com.gkiss01.meetdeb.utils.setAuthToken
import okhttp3.Credentials
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val activityModule = module {
    factory { ActivityViewModel(androidApplication()) }
}

class ActivityViewModel(private val application: Application) : ViewModel() {
    private var _activeUser = MutableLiveData<Resource<User>>()
    val activeUser: LiveData<Resource<User>>
        get() = _activeUser

    fun setActiveUser(user: User) {
        _activeUser.postValue(Resource.success(user))
    }

    private fun resetActiveUser() {
        _activeUser.postValue(Resource.pending(null))
    }

    fun setUserCredentials(username: String?, password: String?) {
        val usernameSafe = username ?: application.getCurrentCredential(CredentialType.EMAIL)
        val passwordSafe = password ?: application.getCurrentCredential(CredentialType.PASSWORD)
        application.setAuthToken(Credentials.basic(usernameSafe, passwordSafe))
    }

    private fun resetUserCredentials() {
        application.setAuthToken()
    }

    fun logout() {
        resetUserCredentials()
        resetActiveUser()
    }
}
