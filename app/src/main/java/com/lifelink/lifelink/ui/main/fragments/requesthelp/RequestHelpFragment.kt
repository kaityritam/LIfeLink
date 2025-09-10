package com.lifelink.lifelink.ui.main.fragments.requesthelp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lifelink.lifelink.R
import com.lifelink.lifelink.data.BloodRequest
import com.lifelink.lifelink.databinding.FragmentRequestHelpBinding
import com.lifelink.lifelink.viewModels.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RequestHelpFragment : Fragment() {

    private var _binding: FragmentRequestHelpBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    private val hospitalList = listOf(
        "Barasat District Hospital", "Barasat Sub Divisional Hospital", "BN Bose Sub Divisional Hospital",
        "Aditya Hospital", "City Life Hospital", "Barasat Health Care Hospital",
        "Medi View Nursing Home", "Care & Cure Hospital", "Disha Eye Hospital (Barasat Unit)",
        "Eskag Sanjeevani Hospital", "Shanti Wellness Care", "KPC Medical College & Hospital (Barasat Unit)",
        "Nabapally Nursing Home", "Life Line Nursing Home", "R B Nursing Home",
        "R B Diagnostics & Nursing Home", "Medi Point Nursing Home", "Mitali Nursing Home"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFormListeners()
        observMyActiveRequest()
    }

    private fun setupFormListeners() {
        setupBloodTypeSpinner()
        setupHospitalAutoComplete()
        setupRadioGroups()
        setupDateTimePickers()
        setupButtonClickListeners()
        setupValidationListeners()
        showEmergencyUI()
        checkFormValidity()
    }
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {

            mainViewModel.fetchMyActiveRequest()
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.fetchMyActiveRequest()
    }


    private fun setupBloodTypeSpinner() {
        val bloodTypes = arrayOf("Select Blood Type", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        binding.spinnerBloodType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, bloodTypes)
    }

    private fun setupHospitalAutoComplete() {
        val hospitalAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, hospitalList)
        binding.etChooseHospital.setAdapter(hospitalAdapter)
    }

    private fun setupRadioGroups() {
        binding.rgUrgency.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.rbEmergency.id -> showEmergencyUI()
                binding.rbOther.id -> showOtherUI()
            }
            checkFormValidity()
        }
        binding.rgOtherOptions.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.rbWithinToday.id -> {
                    binding.layoutWithinToday.visibility = View.VISIBLE
                    binding.layoutDateRange.visibility = View.GONE
                }
                binding.rbDateRange.id -> {
                    binding.layoutDateRange.visibility = View.VISIBLE
                    binding.layoutWithinToday.visibility = View.GONE
                }
            }
            checkFormValidity()
        }
    }

    private fun setupDateTimePickers() {
        val calendar = Calendar.getInstance()
        binding.withinTodaySelectTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                binding.withinTodaySelectTime.text = SimpleDateFormat("hh:mm a", Locale.US).format(calendar.time)
                checkFormValidity()
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }
        binding.tvSelectedDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                binding.tvSelectedDate.text = SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(calendar.time)
                checkFormValidity()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupButtonClickListeners() {
        binding.btnSendEmergency.setOnClickListener {
            val bloodType = binding.spinnerBloodType.selectedItem.toString()
            val hospital = binding.etChooseHospital.text.toString()
            mainViewModel.createNewRequest(bloodType, hospital, "Emergency", "Immediately")

        }
        binding.btnSendOtherToday.setOnClickListener {
            val bloodType = binding.spinnerBloodType.selectedItem.toString()
            val hospital = binding.etChooseHospital.text.toString()
            val selectedTime = binding.withinTodaySelectTime.text.toString()
            mainViewModel.createNewRequest(bloodType, hospital, "Scheduled", "Today by $selectedTime")
        }
        binding.btnSendOtherRange.setOnClickListener {
            val bloodType = binding.spinnerBloodType.selectedItem.toString()
            val hospital = binding.etChooseHospital.text.toString()
            val selectedDate = binding.tvSelectedDate.text.toString()
            mainViewModel.createNewRequest(bloodType, hospital, "Scheduled", "On $selectedDate")
        }

        binding.btnDeleteRequest.setOnClickListener {
            mainViewModel.deleteMyRequest()
        }
    }

    private fun setupValidationListeners() {
        binding.etChooseHospital.addTextChangedListener { checkFormValidity() }
        binding.spinnerBloodType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) { checkFormValidity() }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun showEmergencyUI() {
        binding.layoutEmergency.visibility = View.VISIBLE
        binding.layoutOther.visibility = View.GONE
    }

    private fun showOtherUI() {
        binding.layoutEmergency.visibility = View.GONE
        binding.layoutOther.visibility = View.VISIBLE
        if (binding.rgOtherOptions.checkedRadioButtonId == -1) {
            binding.rbWithinToday.isChecked = true
        }
    }

    private fun checkFormValidity() {
        binding.btnSendEmergency.visibility = View.GONE
        binding.btnSendOtherToday.visibility = View.GONE
        binding.btnSendOtherRange.visibility = View.GONE
        val isBloodTypeSelected = binding.spinnerBloodType.selectedItemPosition > 0
        val isHospitalSelected = hospitalList.contains(binding.etChooseHospital.text.toString())
        if (!isBloodTypeSelected || !isHospitalSelected) return
        when (binding.rgUrgency.checkedRadioButtonId) {
            binding.rbEmergency.id -> {
                binding.btnSendEmergency.visibility = View.VISIBLE
            }
            binding.rbOther.id -> {
                when (binding.rgOtherOptions.checkedRadioButtonId) {
                    binding.rbWithinToday.id -> {
                        if (binding.withinTodaySelectTime.text != "Select time") {
                            binding.btnSendOtherToday.visibility = View.VISIBLE
                        }
                    }
                    binding.rbDateRange.id -> {
                        if (binding.tvSelectedDate.text.isNotBlank() && binding.tvSelectedDate.text.toString() != binding.tvSelectedDate.hint.toString()) {
                            binding.btnSendOtherRange.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }



    private fun showMyRequest(request: BloodRequest?) {
        if (request != null) {
            binding.requestDetailCard.visibility = View.VISIBLE
            binding.requestFromCard.visibility = View.GONE

            binding.tvBloodType.text = "Blood Type: ${request.bloodType}"
            binding.tvHospitalName.text = "Hospital: ${request.hospitalName}"
            binding.tvUrgency.text = "Urgency: ${request.urgency}"
            binding.tvRequestDetails.text = "Details: ${request.details}"

            binding.requestStatus.text = "${request.status}"

            if (request.status == "Accepted") {
                binding.requestStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_accepted_green))
            } else {
                binding.requestStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

        } else {
            binding.requestDetailCard.visibility = View.GONE
            binding.requestFromCard.visibility = View.VISIBLE
        }
    }

    private fun observMyActiveRequest() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.myActiveRequest.collect { activeRequest ->
                    showMyRequest(activeRequest)
                }
            }
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}