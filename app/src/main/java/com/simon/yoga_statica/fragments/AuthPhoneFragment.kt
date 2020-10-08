package com.simon.yoga_statica.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simon.yoga_statica.R
import com.simon.yoga_statica.activies.MainActivity

class AuthPhoneFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_phone_auth, container, false)

        return rootView
    }

    private fun openMain() {
        val intent = Intent(
            activity,
            MainActivity::class.java
        )
        activity?.finish()
        startActivity(intent)
    }
}