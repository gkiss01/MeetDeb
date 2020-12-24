package com.gkiss01.meetdeb.utils

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R

val Fragment.mainActivity: MainActivity?
    get() = activity as? MainActivity

fun Fragment.hideKeyboard() {
    val inputMethodManager = this.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(this.view?.windowToken, 0)
}

@BindingAdapter("setParticipants")
fun TextView.setParticipants(count: Int) {
    text = this.resources.getString(R.string.event_participants, count)
}