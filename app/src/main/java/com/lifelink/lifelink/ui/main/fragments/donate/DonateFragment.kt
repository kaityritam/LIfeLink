package com.lifelink.lifelink.ui.main.fragments.donate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.lifelink.lifelink.databinding.FragmentDonateBinding
import com.lifelink.lifelink.ui.main.fragments.requesthelp.BloodRequestAdapter
import com.lifelink.lifelink.viewModels.MainViewModel
import kotlinx.coroutines.launch

class DonateFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentDonateBinding? = null
    private val binding get() = _binding!!
    private lateinit var requestAdapter: BloodRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDonateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupRefreshListener()
        setupObservers()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.isLoading.collect { isLoading ->
                    binding.swipeRefreshLayout.isRefreshing = isLoading
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.pendingRequests.collect { requests ->
                    if (requests.isEmpty()) {
                        binding.requestsRecyclerView.visibility = View.GONE
                        binding.tvNoRequests.visibility = View.VISIBLE
                    } else {
                        binding.requestsRecyclerView.visibility = View.VISIBLE
                        binding.tvNoRequests.visibility = View.GONE
                        requestAdapter.submitList(requests)
                    }
                }
            }
        }

        mainViewModel.fetchPendingRequests()
    }

    private fun setupRecyclerView() {
        requestAdapter = BloodRequestAdapter { requestToAccept ->
            mainViewModel.acceptBloodRequest(requestToAccept)
            Toast.makeText(requireContext(), "Thank you for accepting!", Toast.LENGTH_SHORT).show()
        }
        binding.requestsRecyclerView.apply {
            adapter = requestAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.isLoading.collect { binding.swipeRefreshLayout.isRefreshing = it }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.pendingRequests.collect { requests ->
                    binding.tvNoRequests.visibility = if (requests.isEmpty()) View.VISIBLE else View.GONE
                    binding.requestsRecyclerView.visibility = if (requests.isEmpty()) View.GONE else View.VISIBLE
                    requestAdapter.submitList(requests)
                }
            }
        }
    }

    private fun setupRefreshListener() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            mainViewModel.fetchPendingRequests()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}