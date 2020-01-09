package com.gkiss01.meetdeb.network

import com.gkiss01.meetdeb.adapter.OffsetDateTimeAdapter
import com.gkiss01.meetdeb.data.GenericResponse
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "http://172.17.172.157:8080"

private val moshi = Moshi.Builder()
    .add(OffsetDateTimeAdapter())
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface WebApiService {
    @GET("events")
    fun getEventsAsync(@Header("Authorization") auth: String): Deferred<GenericResponse>

    @POST("participants/{eventId}")
    fun createParticipantAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Deferred<GenericResponse>

    @DELETE("participants/{eventId}")
    fun deleteParticipantAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Deferred<GenericResponse>
}

object WebApi {
    val retrofitService : WebApiService by lazy {
        retrofit.create(WebApiService::class.java) }
}