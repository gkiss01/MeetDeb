package com.gkiss01.meetdeb.network.api

import com.gkiss01.meetdeb.network.common.PAGE_SIZE
import com.gkiss01.meetdeb.network.common.Resource
import com.gkiss01.meetdeb.network.common.ResourceHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.threeten.bp.OffsetDateTime

class RestClient(private val meetDebService: MeetDebService, private val resourceHandler: ResourceHandler, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    suspend fun createUser(user: RequestBody) = handleRequest(dispatcher) { meetDebService.createUser(user) }
    suspend fun updateUser(auth: String, user: RequestBody) = handleRequest(dispatcher) { meetDebService.updateUser(auth, user) }
    suspend fun deleteUser() = handleRequest(dispatcher) { meetDebService.deleteUser() }
    suspend fun checkUser() = handleRequest(dispatcher) { meetDebService.checkUser() }
    suspend fun getEventsSummary() = handleRequest(dispatcher) { meetDebService.getEventsSummary() }

    suspend fun createEvent(event: MultipartBody.Part, image: MultipartBody.Part?) = handleRequest(dispatcher) { meetDebService.createEvent(event, image) }
    suspend fun updateEvent(event: MultipartBody.Part, image: MultipartBody.Part?) = handleRequest(dispatcher) { meetDebService.updateEvent(event, image) }
    suspend fun deleteEvent(eventId: Long) = handleRequest(dispatcher) { meetDebService.deleteEvent(eventId) }
    suspend fun getEvent(eventId: Long) = handleRequest(dispatcher) { meetDebService.getEvent(eventId) }
    suspend fun getEvents(page: Int) = handleRequest(dispatcher) { meetDebService.getEvents(page, PAGE_SIZE) }
    suspend fun createReport(eventId: Long) = handleRequest(dispatcher) { meetDebService.createReport(eventId) }
    suspend fun deleteReport(eventId: Long) = handleRequest(dispatcher) { meetDebService.deleteReport(eventId) }

    suspend fun modifyParticipation(eventId: Long) = handleRequest(dispatcher) { meetDebService.modifyParticipation(eventId) }
    suspend fun getParticipants(eventId: Long) = handleRequest(dispatcher) { meetDebService.getParticipants(eventId) }

    suspend fun createDate(eventId: Long, date: OffsetDateTime) = handleRequest(dispatcher) { meetDebService.createDate(eventId, date) }
    suspend fun deleteDate(dateId: Long) = handleRequest(dispatcher) { meetDebService.deleteDate(dateId) }
    suspend fun getDates(eventId: Long) = handleRequest(dispatcher) { meetDebService.getDates(eventId) }

    suspend fun changeVote(dateId: Long) = handleRequest(dispatcher) { meetDebService.changeVote(dateId) }

    private suspend fun <T: Any> handleRequest(dispatcher: CoroutineDispatcher, requestFunc: suspend () -> T): Resource<T> {
        return withContext(dispatcher) {
            try {
                resourceHandler.handleSuccess(requestFunc.invoke())
            } catch (e: Exception) {
                resourceHandler.handleException(e)
            }
        }
    }
}