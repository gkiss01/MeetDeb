package com.gkiss01.meetdeb.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import org.threeten.bp.format.DateTimeFormatter

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy, HH:mm")

fun hideKeyboard(context: Context, view: View) {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}
