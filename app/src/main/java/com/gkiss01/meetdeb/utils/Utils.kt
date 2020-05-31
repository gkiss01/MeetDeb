package com.gkiss01.meetdeb.utils

import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.data.isAdmin

fun isActiveUserAdmin() = MainActivity.instance.getActiveUser()!!.isAdmin()