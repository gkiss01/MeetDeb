package com.gkiss01.meetdeb.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gkiss01.meetdeb.adapter.Converters

@Database(entities = [EventEntry::class, ParticipantEntry::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class EventDatabase : RoomDatabase() {

    abstract val eventEntryDao: EventEntryDao
    abstract val participantEntryDao: ParticipantEntryDao

    companion object {

        @Volatile
        private var INSTANCE: EventDatabase? = null

        fun getInstance(context: Context): EventDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        EventDatabase::class.java,
                        "event_entry_database")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}