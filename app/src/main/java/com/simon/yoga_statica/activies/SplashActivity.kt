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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.classes.AdvController
import com.simon.yoga_statica.fragments.AsunaListFragment
import com.simon.yoga_statica.fragments.AuthFragment
import com.simon.yoga_statica.fragments.SplashFragment
import java.lang.Exception

class SplashActivity : AppCompatActivity() {
    private val auth = Firebase.auth
    private lateinit var splashFragment: FrameLayout

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)


        if (!prefs.contains(APP_PREFERENCES_THEME) || auth.currentUser == null) {
            setTheme(R.style.AsunaTheme)
        } else {
            when (prefs.getString(APP_PREFERENCES_THEME, "default")) {
                "default" -> setTheme(R.style.AsunaTheme)
                "red" -> setTheme(R.style.RedAppThemeMin)
                "orange" -> setTheme(R.style.OrangeAppThemeMin)
                "green" -> setTheme(R.style.GreenAppThemeMin)
                "coffee" -> setTheme(R.style.CoffeeAppThemeMin)
            }
        }

        window.setFlags(WindowManager.LayoutParams.FLAGS_CHANGED, WindowManager.LayoutParams.FLAGS_CHANGED)
        setContentView(R.layout.activity_splash)

//        splashFragment
        val authR = intent.getBooleanExtra("auth", false)

        if (authR)
            addAuthFragment()
        else
            startThread()
    }

    private fun startThread() {
        Thread {
            try {
                addSplashFragment()
                Thread.sleep(3000)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (auth.currentUser != null) {
                    finish()
                    startActivity(
                        Intent(
                            this,
                            MainActivity::class.java
                        )
                    )
                } else {
                    Log.d("auth", "NONE")
                    addAuthFragment()
                }
            }
        }.start()
    }

    private fun addSplashFragment() {
        val listFragment = SplashFragment()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        with (transaction) {
            replace(R.id.splashFragment, listFragment)
            commit()
        }

    }

    private fun addAuthFragment() {
        val listFragment = AuthFragment()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        with (transaction) {
            replace(R.id.splashFragment, listFragment)
            commit()
        }

    }

}



