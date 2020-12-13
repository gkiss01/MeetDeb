package com.gkiss01.meetdeb.utils

import androidx.fragment.app.Fragment
import com.gkiss01.meetdeb.MainActivity

val Fragment.mainActivity: MainActivity?
    get() = activity as? MainActivity