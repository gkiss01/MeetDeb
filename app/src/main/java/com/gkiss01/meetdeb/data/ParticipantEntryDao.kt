package com.gkiss01.meetdeb.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ParticipantEntryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(participant: ParticipantEntry)

    @Query("SELECT * FROM participant_entry_table WHERE event_id = :eventId")
    fun getParticipantsByEvent(eventId: Int): LiveData<List<ParticipantEntry>>

    @Query("SELECT COUNT(pid) FROM participant_entry_table WHERE event_id = :eventId")
    fun countParticipantsByEvent(eventId: Int): LiveData<Int>

    @Query("SELECT * FROM participant_entry_table")
    fun getParticipants(): LiveData<List<ParticipantEntry>>
}