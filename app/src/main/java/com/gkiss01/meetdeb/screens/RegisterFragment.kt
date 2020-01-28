package com.gkiss01.meetdeb.screens

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.request.UserRequest
import com.gkiss01.meetdeb.databinding.RegisterFragmentBinding
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import com.gkiss01.meetdeb.network.moshi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RegisterFragment : Fragment() {

    private lateinit var binding: RegisterFragmentBinding

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onErrorReceived(errorCode: ErrorCodes) {
        if (errorCode == ErrorCodes.EMAIL_ALREADY_IN_USE || errorCode == ErrorCodes.BAD_REQUEST_FORMAT) {
            binding.registerButton.hideProgress(R.string.register_title)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationReceived(navigationCode: NavigationCode) {
        if (navigationCode == NavigationCode.NAVIGATE_TO_LOGIN_FRAGMENT) {
            binding.registerButton.hideProgress(R.string.register_created)
            Handler().postDelayed({
                val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
                NavHostFragment.findNavController(this).navigate(action)
            }, 500)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.register_fragment, container, false)
        binding.registerButton.attachTextChangeAnimator()

        binding.alreadyRegistered.setOnClickListener {
            val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }

        binding.registerButton.setOnClickListener {
            var error = false
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val name = binding.name.text.toString()

            if (email.isEmpty()) {
                binding.email.error = "A mezőt kötelező kitölteni!"
                error = true
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.email.error = "Az email cím nem valódi!"
                error = true
            }

            if (password.isEmpty()) {
                binding.password.error = "A mezőt kötelező kitölteni!"
                error = true
            }
            else if (password.length < 8) {
                binding.password.error = "A jelszó min. 8 karakter lehet!"
                error = true
            }

            when {
                name.isEmpty() -> {
                    binding.name.error = "A mezőt kötelező kitölteni!"
                    error = true
                }
                name.length < 4 -> {
                    binding.name.error = "A név min. 4 karakter lehet!"
                    error = true
                }
                name.length > 80 -> {
                    binding.name.error = "A név max. 80 karakter lehet!"
                    error = true
                }
            }

            if (!error) {
                binding.registerButton.showProgress {
                    buttonTextRes = R.string.register_create_waiting
                    progressColor = Color.WHITE
                }

                val inputMethodManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view!!.windowToken, 0)

                val userRequest = UserRequest(email, password, name)
                val json = moshi.adapter(UserRequest::class.java).toJson(userRequest)
                val user = json.toRequestBody("application/json".toMediaTypeOrNull())

                MainActivity.instance.uploadUser(user)
            }
        }

        return binding.root
    }


}