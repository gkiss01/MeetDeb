package com.gkiss01.meetdeb.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import org.threeten.bp.OffsetDateTime

val restModule = module {
    factory { ResourceHandler(get(), androidApplication()) }
    factory { RestClient(get(), get()) }
}

class RestClient(private val dataProvider: DataProvider, private val resourceHandler: ResourceHandler, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    suspend fun createUser(user: RequestBody) = handleRequest(dispatcher) { dataProvider.createUser(user) }
    suspend fun updateUser(auth: String, user: RequestBody) = handleRequest(dispatcher) { dataProvider.updateUser(auth, user) }
    suspend fun deleteUser(auth: String) = handleRequest(dispatcher) { dataProvider.deleteUser(auth) }
    suspend fun checkUser(auth: String) = handleRequest(dispatcher) { dataProvider.checkUser(auth) }
    suspend fun getEventsSummary(auth: String) = handleRequest(dispatcher) { dataProvider.getEventsSummary(auth) }

    suspend fun createEvent(auth: String, event: RequestBody, file: MultipartBody.Part?) = handleRequest(dispatcher) { dataProvider.createEvent(auth, event, file) }
    suspend fun updateEvent(auth: String, event: RequestBody) = handleRequest(dispatcher) { dataProvider.updateEvent(auth, event) }
    suspend fun deleteEvent(eventId: Long) = handleRequest(dispatcher) { dataProvider.deleteEvent(eventId) }
    suspend fun getEvent(eventId: Long) = handleRequest(dispatcher) { dataProvider.getEvent(eventId) }
    suspend fun getEvents(page: Int) = handleRequest(dispatcher) { dataProvider.getEvents(page, PAGE_SIZE) }
    suspend fun createReport(eventId: Long) = handleRequest(dispatcher) { dataProvider.createReport(eventId) }
    suspend fun deleteReport(eventId: Long) = handleRequest(dispatcher) { dataProvider.deleteReport(eventId) }

    suspend fun modifyParticipation(eventId: Long) = handleRequest(dispatcher) { dataProvider.modifyParticipation(eventId) }
    suspend fun getParticipants(auth: String, eventId: Long) = handleRequest(dispatcher) { dataProvider.getParticipants(auth, eventId) }

    suspend fun createDate(auth: String, eventId: Long, date: OffsetDateTime) = handleRequest(dispatcher) { dataProvider.createDate(auth, eventId, date) }
    suspend fun deleteDate(auth: String, dateId: Long) = handleRequest(dispatcher) { dataProvider.deleteDate(auth, dateId) }
    suspend fun getDates(auth: String, eventId: Long) = handleRequest(dispatcher) { dataProvider.getDates(auth, eventId) }

    suspend fun changeVote(auth: String, dateId: Long) = handleRequest(dispatcher) { dataProvider.changeVote(auth, dateId) }

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