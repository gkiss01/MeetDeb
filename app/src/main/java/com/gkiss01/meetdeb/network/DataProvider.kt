package com.gkiss01.meetdeb.network

import com.gkiss01.meetdeb.data.SuccessResponse
import com.gkiss01.meetdeb.data.User
import com.gkiss01.meetdeb.data.fastadapter.Date
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.data.fastadapter.Participant
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.threeten.bp.OffsetDateTime
import retrofit2.http.*

interface DataProvider {
    @GET("users/check")
    suspend fun checkUserAsync(@Header("Authorization") auth: String): User

    @GET("events/{eventId}")
    suspend fun getEventAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Event

    @GET("events")
    suspend fun getEventsAsync(@Header("Authorization") auth: String, @Query("page") page: Int): List<Event>

    @GET("events/reports-add/{eventId}")
    suspend fun reportEventAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): SuccessResponse<Long>

    @GET("events/reports-remove/{eventId}")
    suspend fun removeReportAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): SuccessResponse<Long>

    @GET("dates/{eventId}")
    suspend fun getDatesAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): List<Date>

    @GET("participants/{eventId}")
    suspend fun getParticipantsAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): List<Participant>

    @POST("users")
    suspend fun createUserAsync(@Body user: RequestBody): User

    @Multipart
    @POST("events")
    suspend fun createEventAsync(@Header("Authorization") auth: String, @Part("event") event: RequestBody,
                         @Part file: MultipartBody.Part?): Event

    @Multipart
    @POST("events/update/{eventId}")
    suspend fun updateEventAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long, @Part("event") event: RequestBody): Event

    @POST("participants/{eventId}")
    suspend fun modifyParticipation(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Event

    @POST("votes/{dateId}")
    suspend fun createVoteAsync(@Header("Authorization") auth: String, @Path("dateId") dateId: Long): List<Date>

    @POST("dates/{eventId}")
    suspend fun createDateAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long, @Query("date") date: OffsetDateTime): List<Date>

    @DELETE("events/{eventId}")
    suspend fun deleteEventAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): SuccessResponse<Long>

    @DELETE("dates/{dateId}")
    suspend fun deleteDateAsync(@Header("Authorization") auth: String, @Path("dateId") dateId: Long): SuccessResponse<Long>

    @DELETE("users/{userId}")
    suspend fun deleteUserAsync(@Header("Authorization") auth: String, @Path("userId") userId: Long): SuccessResponse<Long>

//    @Multipart
//    @POST("images/{eventId}")
//    suspend fun uploadImageAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long, @Part file: MultipartBody.Part): GenericResponse
}