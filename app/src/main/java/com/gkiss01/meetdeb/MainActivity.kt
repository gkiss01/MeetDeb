package com.gkiss01.meetdeb

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.gkiss01.meetdeb.data.*
import com.gkiss01.meetdeb.data.adapterrequest.DeleteDateRequest
import com.gkiss01.meetdeb.data.adapterrequest.DeleteEventRequest
import com.gkiss01.meetdeb.data.adapterrequest.DeleteUserRequest
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.network.TargetVar
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.OffsetDateTime
import java.net.ConnectException
import java.net.SocketTimeoutException

class MainActivity : AppCompatActivity() {
    private val viewModelKoin: ActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModelKoin.activeUser.observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> findNavController(R.id.nav_host_fragment).navigate(R.id.eventsFragment)
                Status.ERROR -> {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    findNavController(R.id.nav_host_fragment).navigate(R.id.registerFragment)
                }
                Status.LOADING -> Log.d("MainActivity", "User is loading...")
            }
        })
    }

    companion object {
        lateinit var instance: MainActivity
    }

    init {
        instance = this
    }

    fun getActiveUser(): User? = viewModelKoin.activeUser.value?.data

    fun getEvent(eventId: Long) {
        //makeRequest(dataProvider.getEventAsync(viewModelKoin.basic, eventId), TargetVar.VAR_GET_EVENT)
    }

    fun getEvents(page: Int = 1) {
        //makeRequest(dataProvider.getEventsAsync(viewModelKoin.basic, page), TargetVar.VAR_GET_EVENTS)
    }

    fun createEvent(event: RequestBody, image: MultipartBody.Part?) {
        //makeRequest(dataProvider.createEventAsync(viewModelKoin.basic, event, image), TargetVar.VAR_CREATE_UPDATE_EVENT)
    }

    fun updateEvent(eventId: Long, event: RequestBody) {
        //makeRequest(dataProvider.updateEventAsync(viewModelKoin.basic, eventId, event), TargetVar.VAR_CREATE_UPDATE_EVENT)
    }

    fun deleteEvent(eventId: Long) {
        //makeRequest(dataProvider.deleteEventAsync(viewModelKoin.basic, eventId), TargetVar.VAR_DELETE_EVENT)
    }

    fun reportEvent(eventId: Long) {
        //makeRequest(dataProvider.reportEventAsync(viewModelKoin.basic, eventId), TargetVar.VAR_REPORT_EVENT)
    }

    fun removeReport(eventId: Long) {
        //makeRequest(dataProvider.removeReportAsync(viewModelKoin.basic, eventId), TargetVar.VAR_REMOVE_EVENT_REPORT)
    }

    fun showDates(eventId: Long) {
        //makeRequest(dataProvider.getDatesAsync(viewModelKoin.basic, eventId), TargetVar.VAR_GET_DATES)
    }

    fun createDate(eventId: Long, date: OffsetDateTime) {
        //makeRequest(dataProvider.createDateAsync(viewModelKoin.basic, eventId, date), TargetVar.VAR_CREATE_DATE)
    }

    fun deleteDate(dateId: Long) {
        //makeRequest(dataProvider.deleteDateAsync(viewModelKoin.basic, dateId), TargetVar.VAR_DELETE_DATE)
    }

    fun createVote(dateId: Long) {
        //makeRequest(dataProvider.createVoteAsync(viewModelKoin.basic, dateId), TargetVar.VAR_CREATE_VOTE)
    }

    fun showParticipants(eventId: Long) {
        //makeRequest(dataProvider.getParticipantsAsync(viewModelKoin.basic, eventId), TargetVar.VAR_GET_PARTICIPANTS)
    }

    fun modifyParticipation(eventId: Long, eventAccepted: Boolean) {
        //if (eventAccepted)
            //makeRequest(dataProvider.deleteParticipantAsync(viewModelKoin.basic, eventId), TargetVar.VAR_DELETE_PARTICIPANT)
        //else
            //makeRequest(dataProvider.createParticipantAsync(viewModelKoin.basic, eventId), TargetVar.VAR_CREATE_PARTICIPANT)
    }

    fun createUser(user: RequestBody) {
        //makeRequest(WebApi.retrofitService.createUserAsync(user), TargetVar.VAR_CREATE_USER)
    }

    fun updateUser(auth: String, user: RequestBody) {
        //makeRequest(WebApi.retrofitService.updateUserAsync(auth, user), TargetVar.VAR_UPDATE_USER)
    }

    fun deleteUser(userId: Long) {
        //makeRequest(dataProvider.deleteUserAsync(viewModelKoin.basic, userId), TargetVar.VAR_DELETE_USER)
    }

    private fun makeRequest(target: Deferred<GenericResponse>, targetVar: TargetVar) {
        lifecycleScope.launch {
            try {
                val listResult = target.await()
                if (!listResult.error) {
                    when (targetVar) {
                        //TargetVar.VAR_CHECK_USER -> viewModelKoin.activeUser.value = listResult.user!!
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
                        //TargetVar.VAR_UPDATE_USER -> viewModelKoin.activeUser.value = listResult.user!!
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
