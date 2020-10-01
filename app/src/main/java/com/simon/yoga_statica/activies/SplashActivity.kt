package com.simon.yoga_statica.activies

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import java.lang.Exception

class SplashActivity : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var bottonanimation: TextView
    private val auth = Firebase.auth

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)


        if (!prefs.contains(APP_PREFERENCES_THEME) || auth.currentUser == null) {
            setTheme(R.style.AsunaTheme)
        } else {
            when (prefs.getString(APP_PREFERENCES_THEME, "default")) {
                "coffee" -> setTheme(R.style.CoffeeAppThemeMin)
                "default" -> setTheme(R.style.AsunaTheme)
            }
        }

        window.setFlags(WindowManager.LayoutParams.FLAGS_CHANGED, WindowManager.LayoutParams.FLAGS_CHANGED)
        setContentView(R.layout.activity_splash)

        startThread()
    }

    private fun startThread() {
        Thread {
            try {
                startAnim()
                Thread.sleep(3000)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    )
                )
            }
        }.start()
    }

    private fun startAnim() {
        val image = loadAnimation(this, R.anim.topanimation)
        image.startAnimation(image)

        val bottonanimation = loadAnimation(this, R.anim.topanimation)
        bottonanimation.startAnimation(bottonanimation)
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    private fun Animation.startAnimation(image: Animation?) {
    }
}


