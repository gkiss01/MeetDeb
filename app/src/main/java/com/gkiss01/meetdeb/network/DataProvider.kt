package com.gkiss01.meetdeb.network

import com.gkiss01.meetdeb.data.SuccessResponse
import com.gkiss01.meetdeb.data.User
import com.gkiss01.meetdeb.data.fastadapter.Date
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.data.fastadapter.Participant
import com.gkiss01.meetdeb.data.response.EventSummary
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.threeten.bp.OffsetDateTime
import retrofit2.http.*

interface DataProvider {
    // MARK - Users -

    @POST("users")
    suspend fun createUser(@Body user: RequestBody): User

    @PUT("users")
    suspend fun updateUser(@Header("Authorization") auth: String, @Body user: RequestBody): User

    @DELETE("users")
    suspend fun deleteUser(@Header("Authorization") auth: String): SuccessResponse<Long>

    @GET("users/me")
    suspend fun checkUser(@Header("Authorization") auth: String): User

    @GET("users/summary/events")
    suspend fun getEventsSummary(@Header("Authorization") auth: String): EventSummary

    // MARK - Events -

    @Multipart
    @POST("events")
    suspend fun createEvent(@Header("Authorization") auth: String, @Part("event") event: RequestBody,
                                 @Part file: MultipartBody.Part?): Event

    @Multipart
    @POST("events/update")
    suspend fun updateEvent(@Header("Authorization") auth: String, @Part("event") event: RequestBody): Event

    @DELETE("events/{eventId}")
    suspend fun deleteEvent(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): SuccessResponse<Long>

    @GET("events/{eventId}")
    suspend fun getEvent(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Event

    @GET("events")
    suspend fun getEvents(@Header("Authorization") auth: String, @Query("page") page: Int): List<Event>

    @GET("events/reports/add/{eventId}")
    suspend fun createReport(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): SuccessResponse<Long>

    @GET("events/reports/remove/{eventId}")
    suspend fun deleteReport(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): SuccessResponse<Long>

    // MARK - Participants -

    @POST("participants/{eventId}")
    suspend fun modifyParticipation(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Event

    @GET("participants/{eventId}")
    suspend fun getParticipants(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): List<Participant>

    // MARK - Dates -

    @POST("dates/{eventId}")
    suspend fun createDate(@Header("Authorization") auth: String, @Path("eventId") eventId: Long, @Query("date") date: OffsetDateTime): List<Date>

    @DELETE("dates/{dateId}")
    suspend fun deleteDate(@Header("Authorization") auth: String, @Path("dateId") dateId: Long): SuccessResponse<Long>

    @GET("dates/{eventId}")
    suspend fun getDates(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): List<Date>

    // MARK - Votes -

    @POST("votes/{dateId}")
    suspend fun changeVote(@Header("Authorization") auth: String, @Path("dateId") dateId: Long): List<Date>
}