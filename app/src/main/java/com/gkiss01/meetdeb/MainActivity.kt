package com.gkiss01.meetdeb

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gkiss01.meetdeb.data.DateList
import com.gkiss01.meetdeb.data.EventList
import com.gkiss01.meetdeb.data.GenericResponse
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import com.gkiss01.meetdeb.network.TargetVar
import com.gkiss01.meetdeb.network.WebApi
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import okhttp3.Credentials
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.threeten.bp.OffsetDateTime
import java.net.ConnectException
import java.net.SocketTimeoutException

class MainActivity : AppCompatActivity() {

    private lateinit var basic: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = this.getSharedPreferences("BASIC_AUTH_PREFS", Context.MODE_PRIVATE)
        basic = Credentials.basic(sharedPref.getString("OPTION_EMAIL", "unknown")!!, sharedPref.getString("OPTION_PASSWORD", "unknown")!!)
        checkUser(basic)
    }

    companion object {
        lateinit var instance: MainActivity
    }

    init {
        instance = this
    }

    fun updatePrefs(email: String, password: String) {
        val sharedPref = this.getSharedPreferences("BASIC_AUTH_PREFS", Context.MODE_PRIVATE)
        sharedPref.edit().putString("OPTION_EMAIL", email).putString("OPTION_PASSWORD", password).apply()
        basic = Credentials.basic(email, password)
    }

    fun checkUser(basic: String) {
        makeRequest(WebApi.retrofitService.checkUserAsync(basic), TargetVar.VAR_CHECK_USER)
    }

    fun getEvent(eventId: Long) {
        makeRequest(WebApi.retrofitService.getEventAsync(basic, eventId), TargetVar.VAR_GET_EVENT)
    }

    fun getEvents(page: Int = 1) {
        makeRequest(WebApi.retrofitService.getEventsAsync(basic, page), TargetVar.VAR_GET_EVENTS)
    }

    fun uploadEvent(event: RequestBody, image: MultipartBody.Part?) {
        makeRequest(WebApi.retrofitService.createEventAsync(basic, event, image), TargetVar.VAR_CREATE_EVENT)
    }

    fun showDates(eventId: Long) {
        makeRequest(WebApi.retrofitService.getDatesAsync(basic, eventId), TargetVar.VAR_GET_DATES)
    }

    fun createDate(eventId: Long, date: OffsetDateTime) {
        makeRequest(WebApi.retrofitService.createDateAsync(basic, eventId, date), TargetVar.VAR_CREATE_DATE)
    }

    fun addVote(dateId: Long) {
        makeRequest(WebApi.retrofitService.createVoteAsync(basic, dateId), TargetVar.VAR_CREATE_VOTE)
    }

    fun modifyParticipation(eventId: Long, eventAccepted: Boolean) {
        if (eventAccepted)
            makeRequest(WebApi.retrofitService.deleteParticipantAsync(basic, eventId), TargetVar.VAR_DELETE_PARTICIPANT)
        else
            makeRequest(WebApi.retrofitService.createParticipantAsync(basic, eventId), TargetVar.VAR_CREATE_PARTICIPANT)
    }

    fun uploadUser(user: RequestBody) {
        makeRequest(WebApi.retrofitService.createUserAsync(user), TargetVar.VAR_CREATE_USER)
    }

    private fun makeRequest(target: Deferred<GenericResponse>, targetVar: TargetVar) {
        lifecycleScope.launch {
            try {
                val listResult = target.await()
                if (!listResult.error) {
                    when (targetVar) {
                        TargetVar.VAR_GET_EVENTS -> EventBus.getDefault().post(EventList(listResult.events!!))
                        TargetVar.VAR_CREATE_EVENT, TargetVar.VAR_CHECK_USER -> EventBus.getDefault().post(NavigationCode.NAVIGATE_TO_EVENTS_FRAGMENT)
                        TargetVar.VAR_CREATE_PARTICIPANT, TargetVar.VAR_DELETE_PARTICIPANT, TargetVar.VAR_GET_EVENT -> EventBus.getDefault().post(listResult.event)
                        TargetVar.VAR_GET_DATES, TargetVar.VAR_CREATE_DATE, TargetVar.VAR_CREATE_VOTE -> EventBus.getDefault().post(DateList(listResult.dates ?: emptyList()))
                        TargetVar.VAR_CREATE_USER -> EventBus.getDefault().post(NavigationCode.NAVIGATE_TO_LOGIN_FRAGMENT)
                    }
                }
                else handleResponseErrors(listResult.errorCode!!, listResult.errors!!)
            }
            catch (e: Exception) {
                handleErrors(e)
            }
        }
    }

    private fun handleErrors(e: Exception) {
        val errors = when (e) {
            is SocketTimeoutException -> "Connection error! (server)"
            is ConnectException -> "Connection error! (client)"
            else -> e.message
        }
        Log.d("MainActivityApiCall", "Failure: $errors")
        Log.d("MainActivityApiCall", "$e")
        Toast.makeText(this, errors, Toast.LENGTH_LONG).show()
    }

    private fun handleResponseErrors(errorCode: ErrorCodes, errors: List<String>) {
        var errorsMsg = ""
        Log.d("MainActivityApiCall", "Failure: ${errors.size} errors:")

        EventBus.getDefault().post(errorCode)
        errors.forEachIndexed { index, e  ->
            run {
                Log.d("MainActivityApiCall", e)
                errorsMsg = errorsMsg.plus(if (index == 0) "" else "\n").plus(e)
            }
        }
        Toast.makeText(this, errorsMsg, Toast.LENGTH_LONG).show()
    }
}
