package com.gkiss01.meetdeb

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gkiss01.meetdeb.data.*
import com.gkiss01.meetdeb.data.adapterrequest.DeleteEventRequest
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import com.gkiss01.meetdeb.network.TargetVar
import com.gkiss01.meetdeb.network.WebApi
import com.gkiss01.meetdeb.utils.getSavedPassword
import com.gkiss01.meetdeb.utils.getSavedUsername
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
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //TODO: máshol meghívni ezt, mert így minden elforgatásnál lefut
        if ((application as MainApplication).activeUser == null) checkUser()
    }

    override fun onResume() {
        super.onResume()
        if ((application as MainApplication).activeUser != null) {
            basic = Credentials.basic(getSavedUsername(this), getSavedPassword(this))
            EventBus.getDefault().post(NavigationCode.NAVIGATE_TO_EVENTS_FRAGMENT)
        }
    }

    companion object {
        lateinit var instance: MainActivity
    }

    init {
        instance = this
    }

    fun getActiveUser(): User? = (application as MainApplication).activeUser

    fun checkUser() {
        basic = Credentials.basic(getSavedUsername(this), getSavedPassword(this))
        makeRequest(WebApi.retrofitService.checkUserAsync(basic), TargetVar.VAR_CHECK_USER)
    }

    fun getEvent(eventId: Long) {
        makeRequest(WebApi.retrofitService.getEventAsync(basic, eventId), TargetVar.VAR_GET_EVENT)
    }

    fun getEvents(page: Int = 1) {
        makeRequest(WebApi.retrofitService.getEventsAsync(basic, page), TargetVar.VAR_GET_EVENTS)
    }

    fun createEvent(event: RequestBody, image: MultipartBody.Part?) {
        makeRequest(WebApi.retrofitService.createEventAsync(basic, event, image), TargetVar.VAR_CREATE_UPDATE_EVENT)
    }

    fun updateEvent(eventId: Long, event: RequestBody) {
        makeRequest(WebApi.retrofitService.updateEventAsync(basic, eventId, event), TargetVar.VAR_CREATE_UPDATE_EVENT)
    }

    fun deleteEvent(eventId: Long) {
        makeRequest(WebApi.retrofitService.deleteEventAsync(basic, eventId), TargetVar.VAR_DELETE_EVENT)
    }

    fun reportEvent(eventId: Long) {
        makeRequest(WebApi.retrofitService.reportEventAsync(basic, eventId), TargetVar.VAR_REPORT_EVENT)
    }

    fun removeReport(eventId: Long) {
        makeRequest(WebApi.retrofitService.removeReportAsync(basic, eventId), TargetVar.VAR_REMOVE_EVENT_REPORT)
    }

    fun showDates(eventId: Long) {
        makeRequest(WebApi.retrofitService.getDatesAsync(basic, eventId), TargetVar.VAR_GET_DATES)
    }

    fun showParticipants(eventId: Long) {
        makeRequest(WebApi.retrofitService.getParticipantsAsync(basic, eventId), TargetVar.VAR_GET_PARTICIPANTS)
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
                        TargetVar.VAR_CHECK_USER -> {
                            (application as MainApplication).activeUser = listResult.user!!
                            EventBus.getDefault().post(NavigationCode.NAVIGATE_TO_EVENTS_FRAGMENT)
                        }
                        TargetVar.VAR_GET_EVENTS -> EventBus.getDefault().post(EventList(listResult.events!!))
                        TargetVar.VAR_CREATE_UPDATE_EVENT -> EventBus.getDefault().post(NavigationCode.NAVIGATE_BACK_TO_EVENTS_FRAGMENT)
                        TargetVar.VAR_CREATE_PARTICIPANT, TargetVar.VAR_DELETE_PARTICIPANT, TargetVar.VAR_GET_EVENT, TargetVar.VAR_REMOVE_EVENT_REPORT -> EventBus.getDefault().post(listResult.event)
                        TargetVar.VAR_GET_DATES, TargetVar.VAR_CREATE_DATE, TargetVar.VAR_CREATE_VOTE -> EventBus.getDefault().post(DateList(listResult.dates ?: emptyList()))
                        TargetVar.VAR_CREATE_USER -> EventBus.getDefault().post(NavigationCode.NAVIGATE_TO_LOGIN_FRAGMENT)
                        TargetVar.VAR_GET_PARTICIPANTS -> EventBus.getDefault().post(ParticipantList(listResult.participants!!))
                        TargetVar.VAR_REPORT_EVENT -> Toast.makeText(this@MainActivity, "Event reported!", Toast.LENGTH_LONG).show()
                        TargetVar.VAR_DELETE_EVENT -> EventBus.getDefault().post(DeleteEventRequest(listResult.withId!!))
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
        EventBus.getDefault().post(ErrorCodes.UNKNOWN)
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
        EventBus.getDefault().post(errorCode)

        var errorsMsg = ""
        Log.d("MainActivityApiCall", "Failure: ${errors.size} errors:")
        errors.forEachIndexed { index, e  ->
            run {
                Log.d("MainActivityApiCall", e)
                errorsMsg = errorsMsg.plus(if (index == 0) "" else "\n").plus(e)
            }
        }
        Toast.makeText(this, errorsMsg, Toast.LENGTH_LONG).show()
    }
}
