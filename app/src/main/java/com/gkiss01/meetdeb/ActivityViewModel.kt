package com.gkiss01.meetdeb

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.gkiss01.meetdeb.data.User
import com.gkiss01.meetdeb.data.request.UserRequest
import com.gkiss01.meetdeb.network.Resource
import com.gkiss01.meetdeb.network.RestClient
import com.gkiss01.meetdeb.utils.CredentialType
import com.gkiss01.meetdeb.utils.getCurrentCredential
import com.gkiss01.meetdeb.utils.setAuthToken
import com.squareup.moshi.Moshi
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val activityModule = module {
    factory { ActivityViewModel(get(), get(), androidApplication()) }
}

class ActivityViewModel(private val moshi: Moshi, private val restClient: RestClient, private val application: Application) : ViewModel() {
    private var _activeUser = MutableLiveData<Resource<User>>()
    val activeUser: LiveData<Resource<User>>
        get() = _activeUser

    fun updateUser(currentPassword: String, email: String?, password: String?) = liveData {
        val basic = Credentials.basic(activeUser.value?.data?.email ?: "", currentPassword)

        val userRequest = UserRequest(email, password, null)
        val json = moshi.adapter(UserRequest::class.java).toJson(userRequest)
        val user = json.toRequestBody("application/json".toMediaTypeOrNull())

        emit(Resource.loading(null))
        emit(restClient.updateUser(basic, user))
    }

    fun deleteUser() = liveData {
        emit(Resource.loading(null))
        emit(restClient.deleteUser())
    }

    fun getEventsSummary() = liveData {
        emit(Resource.loading(null))
        emit(restClient.getEventsSummary())
    }

    fun setActiveUser(user: User) {
        _activeUser.postValue(Resource.success(user))
    }

    fun resetActiveUser() {
        _activeUser.postValue(Resource.pending(null))
    }

    fun setUserCredentials(username: String?, password: String?) {
        val usernameSafe = username ?: application.getCurrentCredential(CredentialType.EMAIL)
        val passwordSafe = password ?: application.getCurrentCredential(CredentialType.PASSWORD)
        application.setAuthToken(Credentials.basic(usernameSafe, passwordSafe))
    }

    fun resetUserCredentials() {
        application.setAuthToken()
    }

}
