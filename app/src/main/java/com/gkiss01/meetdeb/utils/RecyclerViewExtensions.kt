package com.gkiss01.meetdeb.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.addOnScrollListener(owner: LifecycleOwner, listener: RecyclerView.OnScrollListener) {
    addOnScrollListener(listener)
    owner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            removeOnScrollListener(listener)
        }
    })
}