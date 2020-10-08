package com.simon.yoga_statica.activies

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.fragments.AuthFragment
import com.simon.yoga_statica.fragments.SplashFragment

class SplashActivity : AppCompatActivity() {
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
                "default" -> setTheme(R.style.AsunaTheme)
                "red" -> setTheme(R.style.RedAppThemeMin)
                "orange" -> setTheme(R.style.OrangeAppThemeMin)
                "green" -> setTheme(R.style.GreenAppThemeMin)
                "coffee" -> setTheme(R.style.CoffeeAppThemeMin)
            }
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAGS_CHANGED,
            WindowManager.LayoutParams.FLAGS_CHANGED
        )
        setContentView(R.layout.activity_splash)

        if (!isOnline()) {
            AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Соединение отсутствует")
                .setMessage("Включите интернет и попробуйте снова!")
                .setPositiveButton("Включить") { _, _ ->
                    val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivityForResult(intent, 5478)
                }
                .setNegativeButton("Обновить") { _, _ ->
                    finish()
                    startActivity(intent)
                }
                .show()
        }

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
                if (isOnline()) {
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
            }
        }.start()
    }

    private fun addSplashFragment() {
        val listFragment = SplashFragment()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        with(transaction) {
            replace(R.id.splashFragment, listFragment)
            commit()
        }

    }

    private fun addAuthFragment() {
        val listFragment = AuthFragment()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        with(transaction) {
            replace(R.id.splashFragment, listFragment)
            commit()
        }

    }

    private fun isOnline() : Boolean {
        var result = false
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }

        return result
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            5478 -> {
                finish()
                startActivity(intent)
            }
        }
    }
}



