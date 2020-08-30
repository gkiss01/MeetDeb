package com.gkiss01.meetdeb

import android.app.Application
import com.gkiss01.meetdeb.network.networkModule
import com.gkiss01.meetdeb.network.restModule
import com.gkiss01.meetdeb.viewmodels.createModule
import com.gkiss01.meetdeb.viewmodels.datesModule
import com.gkiss01.meetdeb.viewmodels.eventsModule
import com.gkiss01.meetdeb.viewmodels.participantsModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MainApplication)
            modules(listOf(networkModule, restModule, activityModule, eventsModule, datesModule, participantsModule, createModule))
        }
    }
}