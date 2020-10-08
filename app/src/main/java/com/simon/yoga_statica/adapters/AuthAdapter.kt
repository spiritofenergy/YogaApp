package com.simon.yoga_statica.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.simon.yoga_statica.fragments.AuthEmailFragment
import com.simon.yoga_statica.fragments.AuthPhoneFragment

class AuthAdapter(
    fm: Fragment,
    private val totalTabs: Int): FragmentStateAdapter(fm) {

    override fun getItemCount(): Int = totalTabs
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                AuthEmailFragment()
            }
            1 -> {
                AuthPhoneFragment()
            }
            else -> createFragment(position)
        }
    }
}