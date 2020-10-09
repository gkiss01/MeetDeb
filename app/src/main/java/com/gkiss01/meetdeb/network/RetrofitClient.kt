package com.gkiss01.meetdeb.network

import android.content.Context
import com.gkiss01.meetdeb.adapter.OffsetDateTimeAdapter
import com.gkiss01.meetdeb.utils.getAuthToken
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

const val BASE_URL = "http://192.168.1.102:8080"
const val PAGE_SIZE = 25

val networkModule = module {
    single { provideInterceptor(get()) }
    single { provideOkHttpClient(get()) }
    single { provideMoshi() }
    single { provideRetrofit(get(), get()) }
    factory { provideApi(get()) }
}

fun provideInterceptor(context: Context): Interceptor = Interceptor {
    val basic = context.getAuthToken()
    val request: Request = it.request().newBuilder()
        .addHeader("Accept", "application/json")
        .addHeader("Authorization", basic)
        .build()
    it.proceed(request)
}

fun provideOkHttpClient(interceptor: Interceptor): OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

fun provideMoshi(): Moshi = Moshi.Builder()
    .add(OffsetDateTimeAdapter())
    .add(KotlinJsonAdapterFactory())
    .build()

fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

fun provideApi(retrofit: Retrofit): DataProvider = retrofit.create(DataProvider::class.java)