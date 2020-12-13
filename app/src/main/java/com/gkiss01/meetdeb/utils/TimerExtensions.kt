package com.gkiss01.meetdeb.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun LifecycleOwner.runDelayed(delay: Long = 500L, actions: () -> Unit) {
    lifecycleScope.launch {
        delay(delay)
        actions()
    }
}