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

class RestClient(private val dataProvider: DataProvider, private val resourceHandler: ResourceHandler, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    suspend fun createUser(user: RequestBody) = handleRequest(dispatcher) { dataProvider.createUser(user) }
    suspend fun updateUser(auth: String, user: RequestBody) = handleRequest(dispatcher) { dataProvider.updateUser(auth, user) }
    suspend fun deleteUser() = handleRequest(dispatcher) { dataProvider.deleteUser() }
    suspend fun checkUser() = handleRequest(dispatcher) { dataProvider.checkUser() }
    suspend fun getEventsSummary() = handleRequest(dispatcher) { dataProvider.getEventsSummary() }

    suspend fun createEvent(event: MultipartBody.Part, image: MultipartBody.Part?) = handleRequest(dispatcher) { dataProvider.createEvent(event, image) }
    suspend fun updateEvent(event: MultipartBody.Part, image: MultipartBody.Part?) = handleRequest(dispatcher) { dataProvider.updateEvent(event, image) }
    suspend fun deleteEvent(eventId: Long) = handleRequest(dispatcher) { dataProvider.deleteEvent(eventId) }
    suspend fun getEvent(eventId: Long) = handleRequest(dispatcher) { dataProvider.getEvent(eventId) }
    suspend fun getEvents(page: Int) = handleRequest(dispatcher) { dataProvider.getEvents(page, PAGE_SIZE) }
    suspend fun createReport(eventId: Long) = handleRequest(dispatcher) { dataProvider.createReport(eventId) }
    suspend fun deleteReport(eventId: Long) = handleRequest(dispatcher) { dataProvider.deleteReport(eventId) }

    suspend fun modifyParticipation(eventId: Long) = handleRequest(dispatcher) { dataProvider.modifyParticipation(eventId) }
    suspend fun getParticipants(eventId: Long) = handleRequest(dispatcher) { dataProvider.getParticipants(eventId) }

    suspend fun createDate(eventId: Long, date: OffsetDateTime) = handleRequest(dispatcher) { dataProvider.createDate(eventId, date) }
    suspend fun deleteDate(dateId: Long) = handleRequest(dispatcher) { dataProvider.deleteDate(dateId) }
    suspend fun getDates(eventId: Long) = handleRequest(dispatcher) { dataProvider.getDates(eventId) }

    suspend fun changeVote(dateId: Long) = handleRequest(dispatcher) { dataProvider.changeVote(dateId) }

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