package com.gkiss01.meetdeb

import android.app.Application
import com.gkiss01.meetdeb.data.User

class MainApplication: Application() {
    var activeUser: User? = null
}