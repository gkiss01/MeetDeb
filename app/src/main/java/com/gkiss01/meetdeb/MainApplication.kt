package com.gkiss01.meetdeb

import android.app.Application
import com.gkiss01.meetdeb.network.common.networkModule
import com.gkiss01.meetdeb.utils.commonModule
import com.gkiss01.meetdeb.viewmodels.eventsModule
import com.gkiss01.meetdeb.viewmodels.profileModule
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
            modules(listOf(commonModule, networkModule, activityModule, profileModule, eventsModule))
        }
    }
}