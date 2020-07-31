package com.gkiss01.meetdeb.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import org.threeten.bp.OffsetDateTime

val restModule = module {
    factory { ResourceHandler(get(), androidApplication()) }
    factory { RestClient(get(), get()) }
}

class RestClient(private val dataProvider: DataProvider, private val resourceHandler: ResourceHandler) {
    suspend fun createUser(user: RequestBody) = handleRequest { dataProvider.createUser(user) }
    suspend fun updateUser(auth: String, user: RequestBody) = handleRequest { dataProvider.updateUser(auth, user) }
    suspend fun deleteUser(auth: String) = handleRequest { dataProvider.deleteUser(auth) }
    suspend fun checkUser(auth: String) = handleRequest { dataProvider.checkUser(auth) }
    suspend fun getEventsSummary(auth: String) = handleRequest { dataProvider.getEventsSummary(auth) }

    suspend fun createEvent(auth: String, event: RequestBody, file: MultipartBody.Part?) = handleRequest { dataProvider.createEvent(auth, event, file) }
    suspend fun updateEvent(auth: String, event: RequestBody) = handleRequest { dataProvider.updateEvent(auth, event) }
    suspend fun deleteEvent(auth: String, eventId: Long) = handleRequest { dataProvider.deleteEvent(auth, eventId) }
    suspend fun getEvent(auth: String, eventId: Long) = handleRequest { dataProvider.getEvent(auth, eventId) }
    suspend fun getEvents(auth: String, page: Int) = handleRequest { dataProvider.getEvents(auth, page) }
    suspend fun createReport(auth: String, eventId: Long) = handleRequest { dataProvider.createReport(auth, eventId) }
    suspend fun deleteReport(auth: String, eventId: Long) = handleRequest { dataProvider.deleteReport(auth, eventId) }

    suspend fun modifyParticipation(auth: String, eventId: Long) = handleRequest { dataProvider.modifyParticipation(auth, eventId) }
    suspend fun getParticipants(auth: String, eventId: Long) = handleRequest { dataProvider.getParticipants(auth, eventId) }

    suspend fun createDate(auth: String, eventId: Long, date: OffsetDateTime) = handleRequest { dataProvider.createDate(auth, eventId, date) }
    suspend fun deleteDate(auth: String, dateId: Long) = handleRequest { dataProvider.deleteDate(auth, dateId) }
    suspend fun getDates(auth: String, eventId: Long) = handleRequest { dataProvider.getDates(auth, eventId) }

    suspend fun changeVote(auth: String, dateId: Long) = handleRequest { dataProvider.changeVote(auth, dateId) }

    private suspend fun <T: Any> handleRequest(requestFunc: suspend () -> T): Resource<T> {
        return try {
            resourceHandler.handleSuccess(requestFunc.invoke())
        } catch (e: Exception) {
            resourceHandler.handleException(e)
        }
    }
}