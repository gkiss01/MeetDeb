package com.gkiss01.meetdeb.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

open class SingleEvent<out T>(private val content: T? = null) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? = if (hasBeenHandled) {
        null
    } else {
        hasBeenHandled = true
        content
    }
}

class VoidEvent {
    private var hasBeenHandled = false

    fun hasBeenHandled(): Boolean = if (hasBeenHandled) {
        true
    } else {
        hasBeenHandled = true
        false
    }
}

fun <T> LiveData<out SingleEvent<T>>.observeEvent(owner: LifecycleOwner, onEventUnhandled: (T) -> Unit) {
    observe(owner) { it?.getContentIfNotHandled()?.let(onEventUnhandled) }
}

fun LiveData<out VoidEvent>.observeEvent(owner: LifecycleOwner, onEventUnhandled: () -> Unit) {
    observe(owner) { if (!it.hasBeenHandled()) onEventUnhandled() }
}