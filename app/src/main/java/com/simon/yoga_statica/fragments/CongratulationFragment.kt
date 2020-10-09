package com.simon.yoga_statica.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simon.yoga_statica.R
import com.simon.yoga_statica.activies.ActionActivity

class CongratulationFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_congratulations, container, false)

        (activity as ActionActivity).setDisplayBack(true)
        (activity as ActionActivity).finish = true

        return rootView
    }
}