package com.simon.yoga_statica.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.simon.yoga_statica.R

class SplashFragment : Fragment() {

    private lateinit var image: ImageView
    private lateinit var bottonanimation: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    private fun startAnim() {
        val image = AnimationUtils.loadAnimation(parentFragment?.context, R.anim.topanimation)
        this.image.startAnimation(image)

        val bottonanimation = AnimationUtils.loadAnimation(parentFragment?.context, R.anim.topanimation)
        this.bottonanimation.startAnimation(bottonanimation)
    }

    private fun Animation.startAnimation(image: Animation?) {
    }
}