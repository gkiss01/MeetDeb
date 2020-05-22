package com.gkiss01.meetdeb.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.koin.dsl.module
import org.threeten.bp.OffsetDateTime

val restModule = module {
    factory { ResourceHandler() }
    factory { RestClient(get(), get()) }
}

class RestClient(private val dataProvider: DataProvider, private val resourceHandler: ResourceHandler) {
    suspend fun checkUserAsync(auth: String) = handleRequest { dataProvider.checkUserAsync(auth) }
    
    suspend fun getEventAsync(auth: String, eventId: Long) = handleRequest { dataProvider.getEventAsync(auth, eventId) }
    
    suspend fun getEventsAsync(auth: String, page: Int) = handleRequest { dataProvider.getEventsAsync(auth, page) }
    
    suspend fun createReportAsync(auth: String, eventId: Long) = handleRequest { dataProvider.createReportAsync(auth, eventId) }
    
    suspend fun deleteReportAsync(auth: String, eventId: Long) = handleRequest { dataProvider.deleteReportAsync(auth, eventId) }
    
    suspend fun getDatesAsync(auth: String, eventId: Long) = handleRequest { dataProvider.getDatesAsync(auth, eventId) }
    
    suspend fun getParticipantsAsync(auth: String, eventId: Long) = handleRequest { dataProvider.getParticipantsAsync(auth, eventId) }
    
    suspend fun createUserAsync(user: RequestBody) = handleRequest { dataProvider.createUserAsync(user) }

    suspend fun createEventAsync(auth: String, event: RequestBody, file: MultipartBody.Part?) = handleRequest { dataProvider.createEventAsync(auth, event, file) }

    suspend fun updateEventAsync(auth: String, eventId: Long, event: RequestBody) = handleRequest { dataProvider.updateEventAsync(auth, eventId, event) }
    
    suspend fun modifyParticipation(auth: String, eventId: Long) = handleRequest { dataProvider.modifyParticipation(auth, eventId) }
    
    suspend fun createVoteAsync(auth: String, dateId: Long) = handleRequest { dataProvider.createVoteAsync(auth, dateId) }
    
    suspend fun createDateAsync(auth: String, eventId: Long, date: OffsetDateTime) = handleRequest { dataProvider.createDateAsync(auth, eventId, date) }

    suspend fun deleteEventAsync(auth: String, eventId: Long) = handleRequest { dataProvider.deleteEventAsync(auth, eventId) }

    suspend fun deleteDateAsync(auth: String, dateId: Long) = handleRequest { dataProvider.deleteDateAsync(auth, dateId) }

    suspend fun deleteUserAsync(auth: String, userId: Long) = handleRequest { dataProvider.deleteUserAsync(auth, userId) }

    private suspend fun <T: Any> handleRequest(requestFunc: suspend () -> T): Resource<T> {
        return try {
            resourceHandler.handleSuccess(requestFunc.invoke())
        } catch (e: Exception) {
            resourceHandler.handleException(e)
        }
    }
}