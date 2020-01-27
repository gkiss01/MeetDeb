package com.gkiss01.meetdeb.screens

import android.content.Context
import android.graphics.Color
import android.os.Bundle
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
import com.gkiss01.meetdeb.databinding.LoginFragmentBinding
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import okhttp3.Credentials
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class LoginFragment : Fragment() {

    private lateinit var binding: LoginFragmentBinding

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
        if (errorCode == ErrorCodes.USER_DISABLED_OR_NOT_VALID) {
            binding.loginButton.hideProgress(R.string.login_title)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationReceived(navigationCode: NavigationCode) {
        if (navigationCode == NavigationCode.NAVIGATE_TO_EVENTS_FRAGMENT) {
            MainActivity.instance.updatePrefs(binding.email.text.toString(), binding.password.text.toString())

            val action = LoginFragmentDirections.actionLoginFragmentToEventsFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.login_fragment, container, false)
        binding.loginButton.attachTextChangeAnimator()

        binding.notRegistered.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }

        binding.loginButton.setOnClickListener {
            var error = false
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

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

            if (!error) {
                binding.loginButton.showProgress {
                    buttonTextRes = R.string.login_waiting
                    progressColor = Color.WHITE
                }

                val inputMethodManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view!!.windowToken, 0)

                val basic = Credentials.basic(email, password)
                MainActivity.instance.checkUser(basic)
            }
        }

        return binding.root
    }
}