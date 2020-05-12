package com.gkiss01.meetdeb.network

import com.gkiss01.meetdeb.data.GenericResponse
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.threeten.bp.OffsetDateTime
import retrofit2.http.*

interface DataProvider {
    @GET("users/check")
    fun checkUserAsync(@Header("Authorization") auth: String): Deferred<GenericResponse>

    @GET("events/{eventId}")
    fun getEventAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Deferred<GenericResponse>

    @GET("events")
    fun getEventsAsync(@Header("Authorization") auth: String, @Query("page") page: Int): Deferred<GenericResponse>

    @GET("events/reports-add/{eventId}")
    fun reportEventAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Deferred<GenericResponse>

    @GET("events/reports-remove/{eventId}")
    fun removeReportAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Deferred<GenericResponse>

    @GET("dates/{eventId}")
    fun getDatesAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Deferred<GenericResponse>

    @GET("participants/{eventId}")
    fun getParticipantsAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Deferred<GenericResponse>

    @POST("users")
    fun createUserAsync(@Body user: RequestBody): Deferred<GenericResponse>

    @Multipart
    @POST("events")
    fun createEventAsync(@Header("Authorization") auth: String, @Part("event") event: RequestBody,
                         @Part file: MultipartBody.Part?): Deferred<GenericResponse>

    @Multipart
    @POST("events/update/{eventId}")
    fun updateEventAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long, @Part("event") event: RequestBody): Deferred<GenericResponse>

    @POST("participants/{eventId}")
    fun createParticipantAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Deferred<GenericResponse>

    @POST("votes/{dateId}")
    fun createVoteAsync(@Header("Authorization") auth: String, @Path("dateId") dateId: Long): Deferred<GenericResponse>

    @POST("dates/{eventId}")
    fun createDateAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long, @Query("date") date: OffsetDateTime): Deferred<GenericResponse>

    @DELETE("events/{eventId}")
    fun deleteEventAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Deferred<GenericResponse>

    @DELETE("dates/{dateId}")
    fun deleteDateAsync(@Header("Authorization") auth: String, @Path("dateId") dateId: Long): Deferred<GenericResponse>

    @DELETE("participants/{eventId}")
    fun deleteParticipantAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Deferred<GenericResponse>

    @DELETE("users/{userId}")
    fun deleteUserAsync(@Header("Authorization") auth: String, @Path("userId") userId: Long): Deferred<GenericResponse>

//    @Multipart
//    @POST("images/{eventId}")
//    fun uploadImageAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long, @Part file: MultipartBody.Part): Deferred<GenericResponse>
}