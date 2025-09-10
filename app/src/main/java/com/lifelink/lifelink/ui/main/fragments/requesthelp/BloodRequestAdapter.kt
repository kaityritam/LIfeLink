package com.lifelink.lifelink.ui.main.fragments.requesthelp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lifelink.lifelink.R
import com.lifelink.lifelink.data.BloodRequest
import com.lifelink.lifelink.databinding.ItemBloodRequestCardBinding
import com.google.android.material.R as MaterialR

class BloodRequestAdapter(
    private val onAcceptClicked: (BloodRequest) -> Unit
) : ListAdapter<BloodRequest, BloodRequestAdapter.RequestViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemBloodRequestCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(getItem(position), onAcceptClicked)
    }

    class RequestViewHolder(private val binding: ItemBloodRequestCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(request: BloodRequest, onAcceptClicked: (BloodRequest) -> Unit) {
            binding.tvBloodTypeDetails.text = "Blood Type: ${request.bloodType}"
            binding.tvHospitalDetails.text = "Hospital: ${request.hospitalName}"
            binding.tvUrgencyDetails.text = "Urgency: ${request.urgency}"
            binding.detailsTextView.text = "Need within : ${request.details}"

            if (request.status == "Pending") {
                binding.acceptButton.text = "Accept"
                binding.acceptButton.isEnabled = true
                binding.acceptButton.backgroundTintList = ContextCompat.getColorStateList(
                    binding.root.context,
                    MaterialR.color.design_default_color_primary
                )
                binding.acceptButton.setOnClickListener { onAcceptClicked(request) }
            } else {
                binding.acceptButton.text = "Accepted"
                binding.acceptButton.isEnabled = false
                binding.acceptButton.backgroundTintList = ContextCompat.getColorStateList(
                    binding.root.context,
                    R.color.status_accepted_green
                )
                binding.acceptButton.setOnClickListener(null)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<BloodRequest>() {
            override fun areItemsTheSame(oldItem: BloodRequest, newItem: BloodRequest) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: BloodRequest, newItem: BloodRequest) = oldItem == newItem
        }
    }
}