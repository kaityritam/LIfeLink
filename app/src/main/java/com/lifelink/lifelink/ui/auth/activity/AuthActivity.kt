package com.lifelink.lifelink.ui.auth.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lifelink.lifelink.api.SupabaseClient
import com.lifelink.lifelink.databinding.ActivityAuthBinding
import com.lifelink.lifelink.ui.auth.fragments.ForgetPasswordFragment
import com.lifelink.lifelink.ui.auth.fragments.RegistrationFragment
import com.lifelink.lifelink.ui.auth.fragments.SignInFragment
import com.lifelink.lifelink.ui.main.MainActivity
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        lifecycleScope.launch {
            delay(3000L)
            val sessionStatus = SupabaseClient.client.auth.sessionStatus
                .filter { it !is SessionStatus.LoadingFromStorage }
                .first()

            if (sessionStatus is SessionStatus.Authenticated) {
                navigateToMainActivity()
            } else {
                if (savedInstanceState == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.authenticationNavHost.id, SignInFragment())
                        .commitNow()
                }
            }
        }
    }

    fun navigateToSignIn() {
        supportFragmentManager.beginTransaction()
            .replace(binding.authenticationNavHost.id, SignInFragment())
            .addToBackStack(null)
            .commit()
    }
    fun navigateToRegistration(){
        supportFragmentManager.beginTransaction()
            .replace(binding.authenticationNavHost.id, RegistrationFragment())
            .addToBackStack(null)
            .commit()
    }
    fun navigateToForgetPassword(){
        supportFragmentManager.beginTransaction()
            .replace(binding.authenticationNavHost.id, ForgetPasswordFragment())
            .addToBackStack(null)
            .commit()
    }

    fun navigateToMainActivity(){
         val intent = Intent(this, MainActivity::class.java)
         startActivity(intent)
         finish()
    }

}