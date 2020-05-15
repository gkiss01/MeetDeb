package com.gkiss01.meetdeb

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.gkiss01.meetdeb.data.User
import com.gkiss01.meetdeb.network.Resource
import com.gkiss01.meetdeb.network.RestClient
import okhttp3.Credentials
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val activityModule = module {
    factory { ActivityViewModel(get(), androidApplication()) }
}

class ActivityViewModel(private val restClient: RestClient, application: Application) : ViewModel() {
    private val username = application.getSavedUsername()
    private val password = application.getSavedPassword()
    private val basic = Credentials.basic(username, password)

    val activeUser: LiveData<Resource<User>> = liveData {
        emit(Resource.loading(null))
        emit(restClient.checkUserAsync(basic))
    }
}

fun Context.getSavedUsername(default: String = "unknown"): String {
    val sharedPref = this.getSharedPreferences("BASIC_AUTH_PREFS", Context.MODE_PRIVATE)
    return sharedPref.getString("OPTION_EMAIL", default)!!
}

fun Context.getSavedPassword(default: String = "unknown"): String {
    val sharedPref = this.getSharedPreferences("BASIC_AUTH_PREFS", Context.MODE_PRIVATE)
    return sharedPref.getString("OPTION_PASSWORD", default)!!
}