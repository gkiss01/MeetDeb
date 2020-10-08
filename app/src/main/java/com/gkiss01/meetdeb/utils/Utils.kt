package com.gkiss01.meetdeb.utils

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.data.isAdmin

val Fragment.mainActivity: MainActivity?
    get() = activity as? MainActivity

// UI események
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
    observe(owner, { it?.getContentIfNotHandled()?.let(onEventUnhandled) })
}

fun LiveData<out VoidEvent>.observeEvent(owner: LifecycleOwner, onEventUnhandled: () -> Unit) {
    observe(owner, { if (!it.hasBeenHandled()) onEventUnhandled() })
}

// NavBackStackEntry
fun <T>Fragment.setNavigationResult(key: String, value: T) {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(key, value)
}

fun <T>Fragment.getNavigationResult(@IdRes id: Int, key: String, onResult: (result: T) -> Unit) {
    val navBackStackEntry = findNavController().getBackStackEntry(id)
    val resumeObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME && navBackStackEntry.savedStateHandle.contains(key)) {
            val result = navBackStackEntry.savedStateHandle.get<T>(key)
            result?.let(onResult)
            navBackStackEntry.savedStateHandle.remove<T>(key)
        }
    }
    navBackStackEntry.lifecycle.addObserver(resumeObserver)
    viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            navBackStackEntry.lifecycle.removeObserver(resumeObserver)
        }
    })
}

fun isActiveUserAdmin() = MainActivity.instance.getActiveUser()!!.isAdmin()