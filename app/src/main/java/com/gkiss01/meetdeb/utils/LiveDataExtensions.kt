package com.gkiss01.meetdeb.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.atomic.AtomicBoolean

class SingleEvent<out T>(private val value: T? = null) {
    private val isConsumed = AtomicBoolean(false)

    internal fun getValueIfNotHandled(): T? =
        if (isConsumed.compareAndSet(false, true)) value
        else null
}

class VoidEvent {
    private val isConsumed = AtomicBoolean(false)

    internal fun hasBeenHandled(): Boolean =
        !isConsumed.compareAndSet(false, true)
}

fun <T> MutableLiveData<SingleEvent<T>>.postEvent(content: T?) {
    postValue(SingleEvent(content))
}

fun MutableLiveData<VoidEvent>.postEvent() {
    postValue(VoidEvent())
}

fun <T> LiveData<out SingleEvent<T>>.observeEvent(owner: LifecycleOwner, onEventUnhandled: (T) -> Unit) {
    observe(owner) { it?.getValueIfNotHandled()?.let(onEventUnhandled) }
}

fun LiveData<out VoidEvent>.observeEvent(owner: LifecycleOwner, onEventUnhandled: () -> Unit) {
    observe(owner) { if (!it.hasBeenHandled()) onEventUnhandled() }
}