package com.lifelink.lifelink.ui.auth.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lifelink.lifelink.R
import com.lifelink.lifelink.databinding.FragmentForgetPasswordBinding

class ForgetPasswordFragment : Fragment() {

    private lateinit var binding : FragmentForgetPasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgetPasswordBinding.inflate(inflater, container, false)

        val newpassword = binding.etNewPassword.text.toString()
        val reenterpassword = binding.etReenterPassword.text.toString()
        val btnResetPassword = binding.btnResetPassword

        binding.backToSignIn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        return binding.root

    }

}