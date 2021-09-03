package com.gkiss01.meetdeb.network.api

import com.gkiss01.meetdeb.network.common.NetworkHandler
import com.gkiss01.meetdeb.network.common.PAGE_SIZE
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.threeten.bp.OffsetDateTime

class RestClient(private val meetDebService: MeetDebService, private val networkHandler: NetworkHandler) {
    suspend fun createUser(user: RequestBody) = networkHandler.safeApiCall { meetDebService.createUser(user) }
    suspend fun updateUser(auth: String, user: RequestBody) = networkHandler.safeApiCall { meetDebService.updateUser(auth, user) }
    suspend fun deleteUser() = networkHandler.safeApiCall { meetDebService.deleteUser() }
    suspend fun checkUser() = networkHandler.safeApiCall { meetDebService.checkUser() }
    suspend fun getEventsSummary() = networkHandler.safeApiCall { meetDebService.getEventsSummary() }

    suspend fun createEvent(event: MultipartBody.Part, image: MultipartBody.Part?) = networkHandler.safeApiCall { meetDebService.createEvent(event, image) }
    suspend fun updateEvent(event: MultipartBody.Part, image: MultipartBody.Part?) = networkHandler.safeApiCall { meetDebService.updateEvent(event, image) }
    suspend fun deleteEvent(eventId: Long) = networkHandler.safeApiCall { meetDebService.deleteEvent(eventId) }
    suspend fun getEvent(eventId: Long) = networkHandler.safeApiCall { meetDebService.getEvent(eventId) }
    suspend fun getEvents(page: Int) = networkHandler.safeApiCall { meetDebService.getEvents(page, PAGE_SIZE) }
    suspend fun createReport(eventId: Long) = networkHandler.safeApiCall { meetDebService.createReport(eventId) }
    suspend fun deleteReport(eventId: Long) = networkHandler.safeApiCall { meetDebService.deleteReport(eventId) }

    suspend fun modifyParticipation(eventId: Long) = networkHandler.safeApiCall { meetDebService.modifyParticipation(eventId) }
    suspend fun getParticipants(eventId: Long) = networkHandler.safeApiCall { meetDebService.getParticipants(eventId) }

    suspend fun createDate(eventId: Long, date: OffsetDateTime) = networkHandler.safeApiCall { meetDebService.createDate(eventId, date) }
    suspend fun deleteDate(dateId: Long) = networkHandler.safeApiCall { meetDebService.deleteDate(dateId) }
    suspend fun getDates(eventId: Long) = networkHandler.safeApiCall { meetDebService.getDates(eventId) }

    suspend fun changeVote(dateId: Long) = networkHandler.safeApiCall { meetDebService.changeVote(dateId) }
}