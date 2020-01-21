package com.gkiss01.meetdeb.network

import com.gkiss01.meetdeb.adapter.OffsetDateTimeAdapter
import com.gkiss01.meetdeb.data.GenericResponse
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

const val BASE_URL = "http://172.17.172.157:8080"

enum class TargetVar {
    VAR_GET_EVENTS, VAR_CREATE_EVENT, VAR_CREATE_PARTICIPANT, VAR_DELETE_PARTICIPANT, VAR_GET_DATES
}

enum class NavigationCode {
    NAVIGATE_TO_EVENTS_FRAGMENT, LOAD_MORE_HAS_ENDED
}

enum class ErrorCode {
    ERROR_NO_EVENTS_FOUND
}

val moshi = Moshi.Builder()
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
    fun getEventsAsync(@Header("Authorization") auth: String, @Query("page") page: Int): Deferred<GenericResponse>

    @GET("dates/{eventId}")
    fun getDatesAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Deferred<GenericResponse>

    @Multipart
    @POST("events")
    fun createEventAsync(@Header("Authorization") auth: String, @Part("event") event: RequestBody,
                         @Part file: MultipartBody.Part?): Deferred<GenericResponse>

    @POST("participants/{eventId}")
    fun createParticipantAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Deferred<GenericResponse>

    @DELETE("participants/{eventId}")
    fun deleteParticipantAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Deferred<GenericResponse>

//    @Multipart
//    @POST("images/{eventId}")
//    fun uploadImageAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long, @Part file: MultipartBody.Part): Deferred<GenericResponse>
}

object WebApi {
    val retrofitService : WebApiService by lazy {
        retrofit.create(WebApiService::class.java) }
}