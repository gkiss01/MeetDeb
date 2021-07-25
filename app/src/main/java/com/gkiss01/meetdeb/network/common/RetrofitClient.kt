package com.gkiss01.meetdeb.network.common

import android.content.Context
import com.gkiss01.meetdeb.network.api.DataProvider
import com.gkiss01.meetdeb.network.api.RestClient
import com.gkiss01.meetdeb.utils.classes.OffsetDateTimeAdapter
import com.gkiss01.meetdeb.utils.getAuthToken
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

const val BASE_URL = "http://192.168.0.101:8080"
const val PAGE_SIZE = 25

val networkModule = module {
    single { provideInterceptor(get()) }
    single { provideOkHttpClient(get()) }
    single { provideMoshi() }
    single { provideRetrofit(get(), get()) }
    factory { provideApi(get()) }
    factory { ResourceHandler(get(), get()) }
    factory { RestClient(get(), get()) }
}

fun provideInterceptor(context: Context): Interceptor = Interceptor {
    val basic = context.getAuthToken()
    val request = it.request().newBuilder().addHeader("Accept", "application/json")
    if (basic.isNotEmpty()) request.addHeader("Authorization", basic)
    it.proceed(request.build())
}

fun provideOkHttpClient(interceptor: Interceptor): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(interceptor)
    .build()

fun provideMoshi(): Moshi = Moshi.Builder()
    .add(OffsetDateTimeAdapter())
    .addLast(KotlinJsonAdapterFactory())
    .build()

fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

fun provideApi(retrofit: Retrofit): DataProvider = retrofit.create(DataProvider::class.java)