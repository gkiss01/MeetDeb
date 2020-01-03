package com.gkiss01.meetdeb.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime

@Entity(tableName = "event_entry_table")
data class EventEntry(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "eid")
    var entryId: Int = 0,

    @ColumnInfo(name = "user_name")
    var userName: String = "",

    @ColumnInfo(name = "date")
    var date: OffsetDateTime? = null,

    @ColumnInfo(name = "venue")
    var venue: String = "",

    @ColumnInfo(name = "labels")
    var labels: String = "",

    @Ignore
    var participants: List<ParticipantEntry>? = null) {

    constructor(eventParticipantDetails: EventParticipantDetails): this(
        eventParticipantDetails.event!!.entryId,
        eventParticipantDetails.event!!.userName,
        eventParticipantDetails.event!!.date,
        eventParticipantDetails.event!!.venue,
        eventParticipantDetails.event!!.labels,
        eventParticipantDetails.participants)
}