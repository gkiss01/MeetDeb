package com.gkiss01.meetdeb.data

import androidx.room.Embedded
import androidx.room.Relation

class EventParticipantDetails {
    @Embedded
    var event: EventEntry? = null

    @Relation(parentColumn = "eid", entityColumn = "event_id", entity = ParticipantEntry::class)
    var participants: List<ParticipantEntry>? = null
}