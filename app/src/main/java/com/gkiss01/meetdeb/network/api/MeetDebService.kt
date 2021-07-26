package com.gkiss01.meetdeb.network.api

import com.gkiss01.meetdeb.data.remote.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.threeten.bp.OffsetDateTime
import retrofit2.http.*

interface MeetDebService {
    // MARK - Users -

    @POST("users")
    suspend fun createUser(@Body user: RequestBody): User

    @PUT("users")
    suspend fun updateUser(@Header("Authorization") auth: String, @Body user: RequestBody): User

    @DELETE("users")
    suspend fun deleteUser(): SuccessResponse<Long>

    @GET("users/me")
    suspend fun checkUser(): User

    @GET("users/summary/events")
    suspend fun getEventsSummary(): EventSummary

    // MARK - Events -

    @Multipart
    @POST("events")
    suspend fun createEvent(@Part event: MultipartBody.Part, @Part image: MultipartBody.Part?): Event

    @Multipart
    @POST("events/update")
    suspend fun updateEvent(@Part event: MultipartBody.Part, @Part image: MultipartBody.Part?): Event

    @DELETE("events/{eventId}")
    suspend fun deleteEvent(@Path("eventId") eventId: Long): SuccessResponse<Long>

    @GET("events/{eventId}")
    suspend fun getEvent(@Path("eventId") eventId: Long): Event

    @GET("events")
    suspend fun getEvents(@Query("page") page: Int, @Query("page") limit: Int): List<Event>

    @GET("events/reports/add/{eventId}")
    suspend fun createReport(@Path("eventId") eventId: Long): SuccessResponse<Long>

    @GET("events/reports/remove/{eventId}")
    suspend fun deleteReport(@Path("eventId") eventId: Long): SuccessResponse<Long>

    // MARK - Participants -

    @POST("participants/{eventId}")
    suspend fun modifyParticipation(@Path("eventId") eventId: Long): Event

    @GET("participants/{eventId}")
    suspend fun getParticipants(@Path("eventId") eventId: Long): List<Participant>

    // MARK - Dates -

    @POST("dates/{eventId}")
    suspend fun createDate(@Path("eventId") eventId: Long, @Query("date") date: OffsetDateTime): List<Date>

    @DELETE("dates/{dateId}")
    suspend fun deleteDate(@Path("dateId") dateId: Long): SuccessResponse<Long>

    @GET("dates/{eventId}")
    suspend fun getDates(@Path("eventId") eventId: Long): List<Date>

    // MARK - Votes -

    @POST("votes/{dateId}")
    suspend fun changeVote(@Path("dateId") dateId: Long): List<Date>
}