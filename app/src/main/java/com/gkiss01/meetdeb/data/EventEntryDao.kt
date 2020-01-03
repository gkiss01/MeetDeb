package com.gkiss01.meetdeb.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import org.threeten.bp.OffsetDateTime

@Dao
interface EventEntryDao {
    @Insert
    fun insert(event: EventEntry)

    @Query("SELECT * FROM event_entry_table WHERE date BETWEEN :from AND :to ORDER BY datetime(date)")
    fun getEventsBetweenDates(from: OffsetDateTime, to: OffsetDateTime): LiveData<List<EventEntry>>

//    @Query("SELECT * FROM event_entry_table ORDER BY datetime(date)")
//    fun getEvents(): LiveData<List<EventEntry>>

    @Transaction
    @Query("SELECT * FROM event_entry_table ORDER BY datetime(date)")
    fun getEvents(): LiveData<List<EventParticipantDetails>>
}