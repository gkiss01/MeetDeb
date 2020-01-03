package com.gkiss01.meetdeb.network

import com.gkiss01.meetdeb.network.data.AlbumProperty
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://jsonplaceholder.typicode.com"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface WebApiService {
    @GET("albums")
    fun getAlbums(): Deferred<List<AlbumProperty>>
}

object WebApi {
    val retrofitService : WebApiService by lazy {
        retrofit.create(WebApiService::class.java) }
}