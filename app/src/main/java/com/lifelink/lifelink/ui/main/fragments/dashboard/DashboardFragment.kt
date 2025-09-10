package com.lifelink.lifelink.ui.main.fragments.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lifelink.lifelink.databinding.FragmentDashboardBinding
import com.lifelink.lifelink.ui.main.MainActivity
import com.lifelink.lifelink.ui.main.fragments.requesthelp.RequestHelpFragment
import com.lifelink.lifelink.viewModels.MainViewModel
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.creteBloodRequestButton.setOnClickListener {
            (requireActivity() as MainActivity).navigateToFragment(RequestHelpFragment(), "REQUEST")
        }

        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.fetchMyActiveRequest()
        mainViewModel.fetchMyAcceptedDonations()
    }

    private fun setupObservers() {
        // Observer 1: For the requester - shows the donor's details
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.myActiveRequest.collect { myRequest ->
                    if (myRequest?.status == "Accepted" && myRequest.donorId != null) {
                        binding.yourBloodRequestAccepted.visibility = View.VISIBLE
                        mainViewModel.fetchDonorProfile(myRequest.donorId)
                    } else {
                        binding.yourBloodRequestAccepted.visibility = View.GONE
                    }
                }
            }
        }

        // Observer 2: For the donor - shows the requester's details
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.myAcceptedDonations.collect { myDonations ->
                    if (myDonations.isNotEmpty()) {
                        val acceptedRequest = myDonations.first()
                        binding.youAcceptedThisBloodRequestCV.visibility = View.VISIBLE
                        mainViewModel.fetchRequesterProfile(acceptedRequest.requesterId)
                    } else {
                        binding.youAcceptedThisBloodRequestCV.visibility = View.GONE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.donorProfile.collect { donor ->
                    if (donor != null) {
                         binding.donersName.text = "Accepted By : ${donor.name}"
                         binding.donersPhoneNumber.text = "Phone Number : ${donor.phone}"
                         binding.callDonner.text = "Call ${donor.name}"
                    }
                }
            }
        }

        // Observer 4: Updates the UI with the requester's profile
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.requesterProfile.collect { requester ->
                    if (requester != null) {
                         binding.requestersNameTextView.text = "Requested By : ${requester.name}"
                         binding.requestersPhoneNumber.text = "Phone Number : ${requester.phone}"
                         binding.callRequestedPersonButton.text = "Call ${requester.name}"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}