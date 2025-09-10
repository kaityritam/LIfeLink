package com.lifelink.lifelink.ui.main.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lifelink.lifelink.databinding.FragmentProfileBinding
import com.lifelink.lifelink.viewModels.AuthViewModel
import com.lifelink.lifelink.ui.auth.activity.AuthActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start observing the user profile state
        observeUserProfile()
        // Trigger the ViewModel to load the data
        authViewModel.fetchUserProfile()
        clickListeners()
    }

    private fun clickListeners(){
        binding.signOutButton.setOnClickListener{
            authViewModel.signOut()
        }
    }

    private fun observeUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Observer 1: For the loading state
                launch {
                    authViewModel.profileLoadingState.collect { state ->
                        Log.d("ProfileFragment_DEBUG", "Profile Loading State changed: $state")
                    }
                }

                // Observer 2: For the user data
                launch {
                    authViewModel.userProfile.collect { user ->
                        if (user != null) {
                            Log.d("ProfileFragment_DEBUG", "User data received, updating UI.")
                            binding.profileName.text = user.name ?: "Not provided"
                            binding.profileEmail.text = user.email ?: "Not provided"
                            binding.profilePhone.text = user.phone ?: "Not provided"
                            binding.bloodGroup.text = user.bloodGroup ?: "Not provided"
                        } else {
                            Log.d("ProfileFragment_DEBUG", "User data is null, showing 'Please log in'.")
                            binding.profileName.text = "Please log in to see your profile."
                            binding.profileEmail.text = ""
                            binding.profilePhone.text = ""
                            binding.bloodGroup.text = ""
                        }
                    }
                }
                launch {
                    authViewModel.logoutState.collect { state ->
                        when (state) {
                            is AuthViewModel.LogoutState.Loading -> {
                                Log.d("ProfileFragment_DEBUG", "Logout Loading")
                            }
                            is AuthViewModel.LogoutState.Success -> {
                                Log.d("ProfileFragment_DEBUG", "Logout Success")
                                val intent = Intent(requireActivity(), AuthActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                requireActivity().finish()

                                // Tell the ViewModel we have handled the event
                                authViewModel.onLogoutStateConsumed()
                            }
                            is AuthViewModel.LogoutState.Error -> {
                                // Handle error state
                            }
                            is AuthViewModel.LogoutState.Idle -> {
                                // Do nothing when the state is idle. This fixes the crash.
                            }
                        }
                    }
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
}