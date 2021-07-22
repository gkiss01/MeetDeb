package com.gkiss01.meetdeb.utils

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.gkiss01.meetdeb.MainActivity

val Fragment.mainActivity get() = activity as? MainActivity

fun Fragment.hideKeyboard() {
    val inputMethodManager = this.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(this.view?.windowToken, 0)
}