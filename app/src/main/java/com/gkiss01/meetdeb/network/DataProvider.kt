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
    // MARK - Users -

    @POST("users")
    suspend fun createUserAsync(@Body user: RequestBody): User

    @PUT("users")
    suspend fun updateUserAsync(@Header("Authorization") auth: String, @Body user: RequestBody): User

    @DELETE("users")
    suspend fun deleteUserAsync(@Header("Authorization") auth: String): SuccessResponse<Long>

    @GET("users/me")
    suspend fun checkUserAsync(@Header("Authorization") auth: String): User

    // MARK - Events -

    @Multipart
    @POST("events")
    suspend fun createEventAsync(@Header("Authorization") auth: String, @Part("event") event: RequestBody,
                                 @Part file: MultipartBody.Part?): Event

    @Multipart
    @POST("events/update")
    suspend fun updateEventAsync(@Header("Authorization") auth: String, @Part("event") event: RequestBody): Event

    @DELETE("events/{eventId}")
    suspend fun deleteEventAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): SuccessResponse<Long>

    @GET("events/{eventId}")
    suspend fun getEventAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Event

    @GET("events")
    suspend fun getEventsAsync(@Header("Authorization") auth: String, @Query("page") page: Int): List<Event>

    @GET("events/reports/add/{eventId}")
    suspend fun createReportAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): SuccessResponse<Long>

    @GET("events/reports/remove/{eventId}")
    suspend fun deleteReportAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): SuccessResponse<Long>

    // MARK - Participants -

    @POST("participants/{eventId}")
    suspend fun modifyParticipation(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): Event

    @GET("participants/{eventId}")
    suspend fun getParticipantsAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): List<Participant>

    // MARK - Dates -

    @POST("dates/{eventId}")
    suspend fun createDateAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long, @Query("date") date: OffsetDateTime): List<Date>

    @DELETE("dates/{dateId}")
    suspend fun deleteDateAsync(@Header("Authorization") auth: String, @Path("dateId") dateId: Long): SuccessResponse<Long>

    @GET("dates/{eventId}")
    suspend fun getDatesAsync(@Header("Authorization") auth: String, @Path("eventId") eventId: Long): List<Date>

    // MARK - Votes -

    @POST("votes/{dateId}")
    suspend fun changeVoteAsync(@Header("Authorization") auth: String, @Path("dateId") dateId: Long): List<Date>
}