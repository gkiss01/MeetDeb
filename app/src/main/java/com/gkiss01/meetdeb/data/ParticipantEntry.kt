package com.gkiss01.meetdeb.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(tableName = "participant_entry_table",
    primaryKeys = ["pid", "event_id"],
    foreignKeys = [ForeignKey(
        entity = EventEntry::class,
        parentColumns = arrayOf("eid"),
        childColumns = arrayOf("event_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class ParticipantEntry(
    @ColumnInfo(name = "pid")
    val participantId: Long,

    @ColumnInfo(name = "event_id", index = true)
    val eventId: Int)