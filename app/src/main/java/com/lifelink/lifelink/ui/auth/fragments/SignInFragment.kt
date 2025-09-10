package com.lifelink.lifelink.ui.auth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.lifelink.lifelink.databinding.FragmentSignInBinding
import com.lifelink.lifelink.viewModels.AuthViewModel
import com.lifelink.lifelink.ui.auth.activity.AuthActivity
import kotlinx.coroutines.launch

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupObservers()
    }

    private fun setupClickListeners() {
        binding.btnSignIn.setOnClickListener {
            loginUser()
        }

        binding.forgetPasswordTextView.setOnClickListener {
            (activity as? AuthActivity)?.navigateToForgetPassword()
            Toast.makeText(requireContext(), "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
        }

        // If you have a sign up prompt text view
        binding.registerPrompt.setOnClickListener {
            (activity as? AuthActivity)?.navigateToRegistration()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            authViewModel.loginState.collect { state ->
                when (state) {
                    is AuthViewModel.LoginState.Loading -> {
//                        showLoading(true)
                        binding.btnSignIn.isEnabled = false
                    }
                    is AuthViewModel.LoginState.Success -> {
//                        showLoading(false)
                        binding.btnSignIn.isEnabled = true
                        Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity()
                    }
                    is AuthViewModel.LoginState.Error -> {
//                        showLoading(false)
                        binding.btnSignIn.isEnabled = true
                        Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
//                        showLoading(false)
                        binding.btnSignIn.isEnabled = true
                    }
                }
            }
        }
    }

    private fun loginUser() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (validateInputs(email, password)) {
            authViewModel.loginUser(email, password)
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.emailEditText.error = "Please enter your email"
            isValid = false
        } else if (!isValidEmail(email)) {
            binding.emailEditText.error = "Please enter a valid email address"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = "Please enter your password"
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)(@)(.+)(\\.)(.+)"
        return email.matches(emailRegex.toRegex())
    }

//    private fun showLoading(show: Boolean) {
//        if (show) {
//            binding.progressBar.visibility = View.VISIBLE
//            binding.signInButton.text = ""
//        } else {
//            binding.progressBar.visibility = View.GONE
//            binding.signInButton.text = "Sign In"
//        }
//    }

    private fun navigateToMainActivity() {
        (activity as? AuthActivity)?.navigateToMainActivity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}