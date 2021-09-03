package com.gkiss01.meetdeb.utils

import org.koin.dsl.module

val commonModule = module {
    single<AuthManager> { AuthManagerImpl(get()) }
}

object Constants {
    const val BASE_URL = "http://192.168.0.102:8080"
    const val PAGING_PAGE_SIZE = 25
    const val PREFERENCES_FILE_NAME = "MEETDEB_PREFS"
    const val PREFERENCES_KEY_AUTH_TOKEN = "AUTH_TOKEN_BASIC"
}