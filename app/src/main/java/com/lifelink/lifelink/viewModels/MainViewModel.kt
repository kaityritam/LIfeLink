package com.lifelink.lifelink.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelink.lifelink.data.BloodRequest
import com.lifelink.lifelink.data.BloodRequestRepository
import com.lifelink.lifelink.data.Profile
import com.lifelink.lifelink.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val requestRepository = BloodRequestRepository()

    private val _pendingRequests = MutableStateFlow<List<BloodRequest>>(emptyList())
    val pendingRequests: StateFlow<List<BloodRequest>> = _pendingRequests

    private val _myActiveRequest = MutableStateFlow<BloodRequest?>(null)
    val myActiveRequest: StateFlow<BloodRequest?> = _myActiveRequest

    private val _myAcceptedDonations = MutableStateFlow<List<BloodRequest>>(emptyList())
    val myAcceptedDonations: StateFlow<List<BloodRequest>> = _myAcceptedDonations

    private val _donorProfile = MutableStateFlow<Profile?>(null)
    val donorProfile: StateFlow<Profile?> = _donorProfile

    private val _requesterProfile = MutableStateFlow<Profile?>(null)
    val requesterProfile: StateFlow<Profile?> = _requesterProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchMyActiveRequest()
        fetchPendingRequests()
        fetchMyAcceptedDonations()
    }

    fun createNewRequest(bloodType: String, hospital: String, urgency: String, details: String) {
        if (_myActiveRequest.value != null) {
            Log.w("MainViewModel", "User already has an active request.")
            return
        }
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            if (userId != null) {
                val newRequest = BloodRequest(requesterId = userId, bloodType = bloodType, hospitalName = hospital, urgency = urgency, details = details)
                val result = requestRepository.createRequest(newRequest)
                if (result.isSuccess) {
                    _myActiveRequest.value = newRequest
                    Log.d("MainViewModel", "createNewRequest SUCCESS: $newRequest")
                } else {
                    Log.e("MainViewModel", "createNewRequest FAILED: ${result.exceptionOrNull()}")
                }
            }
        }
    }

    fun deleteMyRequest() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            if (userId != null) {
                val result = requestRepository.deleteMyRequest(userId)
                if (result.isSuccess) {
                    _myActiveRequest.value = null
                    Log.d("MainViewModel", "deleteMyRequest SUCCESS for userId=$userId")
                } else {
                    Log.e("MainViewModel", "deleteMyRequest FAILED: ${result.exceptionOrNull()}")
                }
            }
        }
    }


    fun acceptBloodRequest(request: BloodRequest) {
        viewModelScope.launch {
            val donorId = userRepository.getCurrentUserId()

            Log.d(
                "Accept_Debug",
                """
            ATTEMPTING TO ACCEPT REQUEST:
            - Current User (Donor) ID: $donorId
            - Request ID being accepted: ${request.id}
            - Request's Original Requester ID: ${request.requesterId}
            - Request's Current Status: ${request.status}
            """.trimIndent()
            )


            if (donorId != null && request.id != null) {
                val result = requestRepository.acceptRequest(request.id, donorId)
                if (result.isSuccess) {
                    Log.i("Accept_Debug", "SUCCESS: Database update reported success.")
                    fetchPendingRequests()
//                    fetchMyActiveRequest()
                    fetchMyAcceptedDonations()
                } else {
                    Log.e("Accept_Debug", "FAILURE: Database update failed.", result.exceptionOrNull())
                }
            }
        }
    }

    fun fetchMyActiveRequest() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            if (userId != null) {
                val result = requestRepository.getMyActiveRequest(userId)
                if (result.isSuccess) {
                    _myActiveRequest.value = result.getOrNull()
                    Log.d("MainViewModel", "fetchMyActiveRequest SUCCESS: ${result.getOrNull()}")
                } else {
                    Log.e("MainViewModel", "fetchMyActiveRequest FAILED: ${result.exceptionOrNull()}")
                }
            }
        }
    }

    fun fetchPendingRequests() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = userRepository.getCurrentUserId()
                if (userId != null) {
                    val result = requestRepository.getPendingRequests(userId)
                    if (result.isSuccess) {
                        _pendingRequests.value = result.getOrNull() ?: emptyList()
                        Log.d("MainViewModel", "fetchPendingRequests SUCCESS: ${_pendingRequests.value.size} requests")
                    } else {
                        Log.e("MainViewModel", "fetchPendingRequests FAILED: ${result.exceptionOrNull()}")
                    }
                } else {
                    _pendingRequests.value = emptyList()
                    Log.w("MainViewModel", "fetchPendingRequests WARN: userId=null")
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchMyAcceptedDonations() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            if (userId != null) {
                val result = requestRepository.getRequestsIAccepted(userId)
                if (result.isSuccess) {
                    _myAcceptedDonations.value = result.getOrNull() ?: emptyList()
                    Log.d("MainViewModel", "fetchMyAcceptedDonations SUCCESS: ${_myAcceptedDonations.value.size} donations")
                } else {
                    Log.e("MainViewModel", "fetchMyAcceptedDonations FAILED: ${result.exceptionOrNull()}")
                }
            }
        }
    }

    fun fetchDonorProfile(donorId: String) {
        viewModelScope.launch {
            val result = requestRepository.getProfile(donorId)
            if (result.isSuccess) {
                _donorProfile.value = result.getOrNull()
                Log.d("MainViewModel", "fetchDonorProfile SUCCESS: ${result.getOrNull()}")
            } else {
                Log.e("MainViewModel", "fetchDonorProfile FAILED: ${result.exceptionOrNull()}")
            }
        }
    }

    fun fetchRequesterProfile(requesterId: String) {
        viewModelScope.launch {
            val result = requestRepository.getProfile(requesterId)
            if (result.isSuccess) {
                _requesterProfile.value = result.getOrNull()
                Log.d("MainViewModel", "fetchRequesterProfile SUCCESS: ${result.getOrNull()}")
            } else {
                Log.e("MainViewModel", "fetchRequesterProfile FAILED: ${result.exceptionOrNull()}")
            }
        }
    }
}
