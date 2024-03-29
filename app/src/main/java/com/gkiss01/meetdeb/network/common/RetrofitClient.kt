package com.gkiss01.meetdeb.network.common

import com.gkiss01.meetdeb.network.api.MeetDebService
import com.gkiss01.meetdeb.network.api.RestClient
import com.gkiss01.meetdeb.utils.AuthManager
import com.gkiss01.meetdeb.utils.Constants
import com.gkiss01.meetdeb.utils.OffsetDateTimeAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { provideInterceptor(get()) }
    single { provideOkHttpClient(get()) }
    single { provideMoshi() }
    single { provideRetrofit(get(), get()) }
    factory { provideApi(get()) }
    factory { NetworkHandler(get(), get()) }
    factory { RestClient(get(), get()) }
}

fun provideInterceptor(authManager: AuthManager): Interceptor = Interceptor {
    val request = it.request().newBuilder().addHeader("Accept", "application/json")
    authManager.getAuthToken().let { basic ->
        if (basic.isNotEmpty()) request.addHeader("Authorization", basic)
    }
    it.proceed(request.build())
}

fun provideOkHttpClient(interceptor: Interceptor): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(interceptor)
    .connectTimeout(1, TimeUnit.MINUTES)
    .readTimeout(1, TimeUnit.MINUTES)
    .writeTimeout(1, TimeUnit.MINUTES)
    .build()

fun provideMoshi(): Moshi = Moshi.Builder()
    .add(OffsetDateTimeAdapter())
    .addLast(KotlinJsonAdapterFactory())
    .build()

fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
    .baseUrl(Constants.BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

fun provideApi(retrofit: Retrofit): MeetDebService = retrofit.create(MeetDebService::class.java)