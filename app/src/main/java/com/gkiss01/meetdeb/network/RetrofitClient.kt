package com.gkiss01.meetdeb.network

import com.gkiss01.meetdeb.adapter.OffsetDateTimeAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

const val BASE_URL = "http://192.168.1.106:8080"

enum class TargetVar {
    VAR_GET_EVENTS, VAR_GET_EVENT, VAR_CREATE_UPDATE_EVENT, VAR_DELETE_EVENT,
    VAR_REPORT_EVENT, VAR_REMOVE_EVENT_REPORT,
    VAR_GET_PARTICIPANTS, VAR_CREATE_PARTICIPANT, VAR_DELETE_PARTICIPANT,
    VAR_GET_DATES, VAR_CREATE_DATE, VAR_DELETE_DATE,
    VAR_CREATE_VOTE,
    VAR_CREATE_USER, VAR_DELETE_USER, VAR_CHECK_USER
}

enum class NavigationCode {
    ACTIVE_USER_UPDATED,
    NAVIGATE_TO_EVENTS_FRAGMENT,
    NAVIGATE_TO_LOGIN_FRAGMENT,
    NAVIGATE_TO_IMAGE_PICKER
}

enum class ErrorCodes {
    USER_DISABLED_OR_NOT_VALID,
    ACCESS_DENIED,
    BAD_REQUEST_FORMAT,
    UNKNOWN,

    USER_NOT_FOUND,
    NO_USERS_FOUND,
    CONFIRMATION_TOKEN_NOT_FOUND,
    USER_ALREADY_VERIFIED,
    EMAIL_ALREADY_IN_USE,

    EVENT_NOT_FOUND,
    NO_EVENTS_FOUND,
    PARTICIPANT_NOT_FOUND,
    NO_PARTICIPANTS_FOUND,

    PARTICIPANT_ALREADY_CREATED,
    DATE_NOT_FOUND,
    NO_DATES_FOUND,
    DATE_ALREADY_CREATED,

    VOTE_NOT_FOUND,
    VOTE_ALREADY_CREATED,

    FILE_NOT_FOUND,
    FILENAME_INVALID,
    COULD_NOT_CONVERT_IMAGE,
    FILE_SIZE_LIMIT_EXCEEDED,
    UPLOAD_FAILED,
    COULD_NOT_CREATE_DIRECTORY;
}

val networkModule = module {
    single { provideInterceptor() }
    single { provideOkHttpClient(get()) }
    single { provideMoshi() }
    single { provideRetrofit(get(), get()) }
    factory { provideApi(get()) }
}

fun provideInterceptor(): Interceptor {
    return Interceptor {
        val request: Request = it.request().newBuilder().addHeader("Accept", "application/json").build()
        it.proceed(request)
    }
}

fun provideOkHttpClient(interceptor: Interceptor): OkHttpClient {
    return OkHttpClient.Builder().addInterceptor(interceptor).build()
}

fun provideMoshi(): Moshi {
    return Moshi.Builder()
        .add(OffsetDateTimeAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()
}

fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
    return Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
}

fun provideApi(retrofit: Retrofit): DataProvider = retrofit.create(DataProvider::class.java)