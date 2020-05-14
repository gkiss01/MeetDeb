package com.gkiss01.meetdeb.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.koin.dsl.module
import org.threeten.bp.OffsetDateTime

val restModule = module {
    factory { RestClient(get()) }
}

class RestClient(private val dataProvider: DataProvider) {
    suspend fun checkUserAsync(auth: String) = dataProvider.checkUserAsync(auth)
    
    suspend fun getEventAsync(auth: String, eventId: Long) = dataProvider.getEventAsync(auth, eventId)
    
    suspend fun getEventsAsync(auth: String, page: Int) = dataProvider.getEventsAsync(auth, page)
    
    suspend fun reportEventAsync(auth: String, eventId: Long) = dataProvider.reportEventAsync(auth, eventId)
    
    suspend fun removeReportAsync(auth: String, eventId: Long) = dataProvider.removeReportAsync(auth, eventId)
    
    suspend fun getDatesAsync(auth: String, eventId: Long) = dataProvider.getDatesAsync(auth, eventId)
    
    suspend fun getParticipantsAsync(auth: String, eventId: Long) = dataProvider.getParticipantsAsync(auth, eventId)
    
    suspend fun createUserAsync(user: RequestBody) = dataProvider.createUserAsync(user)

    suspend fun createEventAsync(auth: String, event: RequestBody, file: MultipartBody.Part?) = dataProvider.createEventAsync(auth, event, file)

    suspend fun updateEventAsync(auth: String, eventId: Long, event: RequestBody) = dataProvider.updateEventAsync(auth, eventId, event)
    
    suspend fun createParticipantAsync(auth: String, eventId: Long) = dataProvider.createParticipantAsync(auth, eventId)
    
    suspend fun createVoteAsync(auth: String, dateId: Long) = dataProvider.createVoteAsync(auth, dateId)
    
    suspend fun createDateAsync(auth: String, eventId: Long, date: OffsetDateTime) = dataProvider.createDateAsync(auth, eventId, date)

    suspend fun deleteEventAsync(auth: String, eventId: Long) = dataProvider.deleteEventAsync(auth, eventId)

    suspend fun deleteDateAsync(auth: String, dateId: Long) = dataProvider.deleteDateAsync(auth, dateId)

    suspend fun deleteParticipantAsync(auth: String, eventId: Long) = dataProvider.deleteParticipantAsync(auth, eventId)

    suspend fun deleteUserAsync(auth: String, userId: Long) = dataProvider.deleteUserAsync(auth, userId)
}