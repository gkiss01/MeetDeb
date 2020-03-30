package com.gkiss01.meetdeb

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.gkiss01.meetdeb.data.*
import com.gkiss01.meetdeb.data.adapterrequest.DeleteDateRequest
import com.gkiss01.meetdeb.data.adapterrequest.DeleteEventRequest
import com.gkiss01.meetdeb.data.adapterrequest.DeleteUserRequest
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import com.gkiss01.meetdeb.network.TargetVar
import com.gkiss01.meetdeb.network.WebApi
import com.gkiss01.meetdeb.utils.getSavedPassword
import com.gkiss01.meetdeb.utils.getSavedUsername
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.threeten.bp.OffsetDateTime
import java.net.ConnectException
import java.net.SocketTimeoutException

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)

        if (viewModel.activeUser.value == null)
            checkUser()

        viewModel.activeUser.observe(this, Observer {
            EventBus.getDefault().post(NavigationCode.ACTIVE_USER_UPDATED)
        })
    }

    companion object {
        lateinit var instance: MainActivity
    }

    init {
        instance = this
    }

    fun saveTempPassword(password: String) {
        viewModel.tempPassword = password
    }
    fun getTempPassword(): String = viewModel.tempPassword

    fun recalculateBasic() {
        viewModel.recalculateBasic(getSavedUsername(this), getSavedPassword(this))
    }

    fun getActiveUser(): User? = viewModel.activeUser.value

    fun checkUser() {
        recalculateBasic()
        makeRequest(WebApi.retrofitService.checkUserAsync(viewModel.basic), TargetVar.VAR_CHECK_USER)
    }

    fun getEvent(eventId: Long) {
        makeRequest(WebApi.retrofitService.getEventAsync(viewModel.basic, eventId), TargetVar.VAR_GET_EVENT)
    }

    fun getEvents(page: Int = 1) {
        makeRequest(WebApi.retrofitService.getEventsAsync(viewModel.basic, page), TargetVar.VAR_GET_EVENTS)
    }

    fun createEvent(event: RequestBody, image: MultipartBody.Part?) {
        makeRequest(WebApi.retrofitService.createEventAsync(viewModel.basic, event, image), TargetVar.VAR_CREATE_UPDATE_EVENT)
    }

    fun updateEvent(eventId: Long, event: RequestBody) {
        makeRequest(WebApi.retrofitService.updateEventAsync(viewModel.basic, eventId, event), TargetVar.VAR_CREATE_UPDATE_EVENT)
    }

    fun deleteEvent(eventId: Long) {
        makeRequest(WebApi.retrofitService.deleteEventAsync(viewModel.basic, eventId), TargetVar.VAR_DELETE_EVENT)
    }

    fun reportEvent(eventId: Long) {
        makeRequest(WebApi.retrofitService.reportEventAsync(viewModel.basic, eventId), TargetVar.VAR_REPORT_EVENT)
    }

    fun removeReport(eventId: Long) {
        makeRequest(WebApi.retrofitService.removeReportAsync(viewModel.basic, eventId), TargetVar.VAR_REMOVE_EVENT_REPORT)
    }

    fun showDates(eventId: Long) {
        makeRequest(WebApi.retrofitService.getDatesAsync(viewModel.basic, eventId), TargetVar.VAR_GET_DATES)
    }

    fun createDate(eventId: Long, date: OffsetDateTime) {
        makeRequest(WebApi.retrofitService.createDateAsync(viewModel.basic, eventId, date), TargetVar.VAR_CREATE_DATE)
    }

    fun deleteDate(dateId: Long) {
        makeRequest(WebApi.retrofitService.deleteDateAsync(viewModel.basic, dateId), TargetVar.VAR_DELETE_DATE)
    }

    fun createVote(dateId: Long) {
        makeRequest(WebApi.retrofitService.createVoteAsync(viewModel.basic, dateId), TargetVar.VAR_CREATE_VOTE)
    }

    fun showParticipants(eventId: Long) {
        makeRequest(WebApi.retrofitService.getParticipantsAsync(viewModel.basic, eventId), TargetVar.VAR_GET_PARTICIPANTS)
    }

    fun modifyParticipation(eventId: Long, eventAccepted: Boolean) {
        if (eventAccepted)
            makeRequest(WebApi.retrofitService.deleteParticipantAsync(viewModel.basic, eventId), TargetVar.VAR_DELETE_PARTICIPANT)
        else
            makeRequest(WebApi.retrofitService.createParticipantAsync(viewModel.basic, eventId), TargetVar.VAR_CREATE_PARTICIPANT)
    }

    fun createUser(user: RequestBody) {
        makeRequest(WebApi.retrofitService.createUserAsync(user), TargetVar.VAR_CREATE_USER)
    }

    fun updateUser(auth: String, user: RequestBody) {
        makeRequest(WebApi.retrofitService.updateUserAsync(auth, user), TargetVar.VAR_UPDATE_USER)
    }

    fun deleteUser(userId: Long) {
        makeRequest(WebApi.retrofitService.deleteUserAsync(viewModel.basic, userId), TargetVar.VAR_DELETE_USER)
    }

    private fun makeRequest(target: Deferred<GenericResponse>, targetVar: TargetVar) {
        lifecycleScope.launch {
            try {
                val listResult = target.await()
                if (!listResult.error) {
                    when (targetVar) {
                        TargetVar.VAR_CHECK_USER -> viewModel.activeUser.value = listResult.user!!
                        TargetVar.VAR_GET_EVENTS -> EventBus.getDefault().post(EventList(listResult.events!!))
                        TargetVar.VAR_CREATE_UPDATE_EVENT -> EventBus.getDefault().post(NavigationCode.NAVIGATE_TO_EVENTS_FRAGMENT)
                        TargetVar.VAR_CREATE_PARTICIPANT, TargetVar.VAR_DELETE_PARTICIPANT, TargetVar.VAR_GET_EVENT, TargetVar.VAR_REMOVE_EVENT_REPORT -> EventBus.getDefault().post(listResult.event)
                        TargetVar.VAR_GET_DATES, TargetVar.VAR_CREATE_DATE, TargetVar.VAR_CREATE_VOTE -> EventBus.getDefault().post(DateList(listResult.dates ?: emptyList()))
                        TargetVar.VAR_CREATE_USER -> EventBus.getDefault().post(NavigationCode.NAVIGATE_TO_LOGIN_FRAGMENT)
                        TargetVar.VAR_GET_PARTICIPANTS -> EventBus.getDefault().post(ParticipantList(listResult.participants!!))
                        TargetVar.VAR_REPORT_EVENT -> Toast.makeText(this@MainActivity, "Event reported!", Toast.LENGTH_LONG).show()
                        TargetVar.VAR_DELETE_EVENT -> EventBus.getDefault().post(DeleteEventRequest(listResult.withId!!))
                        TargetVar.VAR_DELETE_DATE -> EventBus.getDefault().post(DeleteDateRequest(listResult.withId!!))
                        TargetVar.VAR_DELETE_USER -> EventBus.getDefault().post(DeleteUserRequest())
                        TargetVar.VAR_UPDATE_USER -> {
                            viewModel.activeUser.value = listResult.user!!
                            viewModel.recalculateBasic(getSavedPassword(this@MainActivity))
                        }
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
