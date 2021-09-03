package com.gkiss01.meetdeb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gkiss01.meetdeb.data.remote.response.User
import com.gkiss01.meetdeb.network.common.Resource
import com.gkiss01.meetdeb.utils.AuthManager
import okhttp3.Credentials
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val activityModule = module {
    viewModel { ActivityViewModel(get()) }
}

class ActivityViewModel(private val authManager: AuthManager) : ViewModel() {
    private var _activeUser = MutableLiveData<Resource<User>>()
    val activeUser: LiveData<Resource<User>>
        get() = _activeUser

    fun setActiveUser(user: User) {
        _activeUser.postValue(Resource.success(user))
    }

    private fun resetActiveUser() {
        _activeUser.postValue(null)
    }

    fun setUserCredentials(username: String?, password: String?) {
        val usernameSafe = username ?: authManager.getCredential(AuthManager.CredentialType.EMAIL) ?: ""
        val passwordSafe = password ?: authManager.getCredential(AuthManager.CredentialType.PASSWORD) ?: ""
        authManager.setAuthToken(Credentials.basic(usernameSafe, passwordSafe))
    }

    private fun resetUserCredentials() {
        authManager.setAuthToken(null)
    }

    fun logout() {
        resetUserCredentials()
        resetActiveUser()
    }
}
