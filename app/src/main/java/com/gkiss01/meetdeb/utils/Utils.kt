package com.gkiss01.meetdeb.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.data.Role
import com.gkiss01.meetdeb.data.User
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy, HH:mm")

fun getActiveUser() = MainActivity.instance.getActiveUser()
fun isActiveUserAdmin() = MainActivity.instance.getActiveUser()?.roles?.contains(Role.ROLE_ADMIN)
fun isActiveUserAdmin(user: User) = user.roles.contains(Role.ROLE_ADMIN)

fun getSavedUsername(context: Context, default: String = "unknown"): String {
    val sharedPref = context.getSharedPreferences("BASIC_AUTH_PREFS", Context.MODE_PRIVATE)
    return sharedPref.getString("OPTION_EMAIL", default)!!
}

fun getSavedPassword(context: Context, default: String = "unknown"): String {
    val sharedPref = context.getSharedPreferences("BASIC_AUTH_PREFS", Context.MODE_PRIVATE)
    return sharedPref.getString("OPTION_PASSWORD", default)!!
}

fun setSavedUser(context: Context, username: String, password: String) {
    val sharedPref = context.getSharedPreferences("BASIC_AUTH_PREFS", Context.MODE_PRIVATE)
    sharedPref.edit().putString("OPTION_EMAIL", username).putString("OPTION_PASSWORD", password).apply()
}

fun hideKeyboard(context: Context, view: View) {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun formatDate(offsetDateTime: OffsetDateTime): String = offsetDateTime.format(dateFormatter)

fun updateOffsetDateTime(offsetDateTime: OffsetDateTime, year: Int, monthValue: Int, dayOfMonth: Int): OffsetDateTime {
    return OffsetDateTime.of(year, monthValue, dayOfMonth, offsetDateTime.hour, offsetDateTime.minute,
        offsetDateTime.second, offsetDateTime.nano, offsetDateTime.offset)
}

fun updateOffsetDateTime(offsetDateTime: OffsetDateTime, hourOfDay: Int, minute: Int): OffsetDateTime {
    return OffsetDateTime.of(offsetDateTime.year, offsetDateTime.monthValue, offsetDateTime.dayOfMonth, hourOfDay, minute,
        offsetDateTime.second, offsetDateTime.nano, offsetDateTime.offset)
}

fun isDate24HourFormat(context: Context) = android.text.format.DateFormat.is24HourFormat(context)