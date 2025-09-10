package com.lifelink.lifelink.ui.auth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.lifelink.lifelink.databinding.FragmentRegistrationBinding
import com.lifelink.lifelink.viewModels.AuthViewModel
import com.lifelink.lifelink.ui.auth.activity.AuthActivity
import kotlinx.coroutines.launch

class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()
        setupClickListeners()
        setupObservers()
    }

    private fun setupSpinner() {
        val bloodGroups = listOf("Select Blood Group", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bloodGroups)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.bloodGroupSpinner.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.signInPrompt.setOnClickListener {
            (activity as? AuthActivity)?.navigateToSignIn()
        }

        binding.registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            authViewModel.registrationState.collect { state ->
                when (state) {
                    is AuthViewModel.RegistrationState.Loading -> {
//                        showLoading(true)
                        binding.registerButton.isEnabled = false
                    }
                    is AuthViewModel.RegistrationState.Success -> {
//                        showLoading(false)
                        binding.registerButton.isEnabled = true
                        Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                        (activity as? AuthActivity)?.navigateToSignIn()
                    }
                    is AuthViewModel.RegistrationState.Error -> {
//                        showLoading(false)
                        binding.registerButton.isEnabled = true

                        if (state.message.contains("already registered") ||
                            state.message.contains("Registration completed")) {
                            // If user already exists, suggest signing in
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            (activity as? AuthActivity)?.navigateToSignIn()
                        } else {
                            Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
//                        showLoading(false)
                        binding.registerButton.isEnabled = true
                    }
                }
            }
        }
    }

    private fun registerUser() {
        val name = binding.nameEditText.text.toString().trim()
        val phone = binding.phoneEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val bloodGroup = binding.bloodGroupSpinner.selectedItem.toString()

        if (validateInputs(name, phone, email, password, bloodGroup)) {
            authViewModel.registerUser(email, password, name, phone, bloodGroup)
        }
    }

    private fun validateInputs(
        name: String,
        phone: String,
        email: String,
        password: String,
        bloodGroup: String
    ): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.nameEditText.error = "Please enter your full name"
            isValid = false
        }

        if (phone.isEmpty()) {
            binding.phoneEditText.error = "Please enter your phone number"
            isValid = false
        } else if (!isValidPhone(phone)) {
            binding.phoneEditText.error = "Please enter a valid phone number"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.emailEditText.error = "Please enter your email"
            isValid = false
        } else if (!isValidEmail(email)) {
            binding.emailEditText.error = "Please enter a valid email address"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = "Please enter a password"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordEditText.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (bloodGroup == "Select Blood Group") {
            Toast.makeText(requireContext(), "Please select your blood group", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)(@)(.+)(\\.)(.+)"
        return email.matches(emailRegex.toRegex())
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.length >= 10 && phone.all { it.isDigit() }
    }

//    private fun showLoading(show: Boolean) {
//        if (show) {
//            binding.progressBar.visibility = View.VISIBLE
//            binding.registerButton.text = ""
//        } else {
//            binding.progressBar.visibility = View.GONE
//            binding.registerButton.text = "Register"
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}