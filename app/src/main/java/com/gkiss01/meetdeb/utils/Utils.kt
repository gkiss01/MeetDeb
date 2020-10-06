package com.gkiss01.meetdeb.utils

import androidx.fragment.app.Fragment
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.data.isAdmin

val Fragment.mainActivity: MainActivity?
    get() = activity as? MainActivity

fun isActiveUserAdmin() = MainActivity.instance.getActiveUser()!!.isAdmin()