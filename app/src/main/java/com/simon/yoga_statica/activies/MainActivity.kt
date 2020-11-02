package com.simon.yoga_statica.activies

import androidx.fragment.app.Fragment
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManagerNonConfig
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.classes.AdvController
import com.simon.yoga_statica.fragments.AsunaFragment
import com.simon.yoga_statica.fragments.AsunaListFragment
import com.simon.yoga_statica.fragments.FavoriteListFragment
import com.simon.yoga_statica.fragments.ProfileFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var gso: GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var auth: FirebaseAuth

    private lateinit var advController: AdvController
    private lateinit var inter: InterstitialAd

    private lateinit var container: FrameLayout

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        auth = Firebase.auth
        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)

        if (!prefs.contains(APP_PREFERENCES_THEME) || auth.currentUser == null) {
            setTheme(R.style.AppTheme)
        } else {
            when (prefs.getString(APP_PREFERENCES_THEME, "default")) {

                "default" -> setTheme(R.style.AppTheme)
                "red" -> setTheme(R.style.RedAppTheme)
                "orange" -> setTheme(R.style.OrangeAppTheme)
                "lime" -> setTheme(R.style.LimeAppTheme)
                "coffee" -> setTheme(R.style.CoffeeAppTheme)

            }
        }
        setContentView(R.layout.activity_main)
        val count = supportFragmentManager.backStackEntryCount
        Log.d("c", count.toString())
        if (count == 0)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        else
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val profile = intent.getBooleanExtra("profile", false)
        intent.putExtra("profile", false)

        Log.d("lang", Locale.getDefault().displayLanguage)

        container = findViewById(R.id.fragmentContainer)

        advController = AdvController(this)
        advController.init()

        inter = advController.createInterstitialAds(R.string.ads_inter_uid)

        if (count == 0) {
            inter.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    showAdv()
                }
            }
            openMain()
        }

        if (profile) {
            Log.d("prof", "true")
            openProfile()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_main, menu)

        if (auth.currentUser != null) {
            menu?.getItem(0)?.isVisible = true
            menu?.getItem(1)?.isVisible = true
            menu?.getItem(2)?.isVisible = true
            menu?.getItem(3)?.isVisible = true
        }

        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val count = supportFragmentManager.backStackEntryCount
        Log.d("c", count.toString())
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.favoriteBut -> {
                if (getCurFragment() != "favorite") {
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    val listFragment = FavoriteListFragment()
                    val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                    if (container.tag == "usual_display") {
                        with(transaction) {
                            replace(R.id.fragmentContainer, listFragment)
                            addToBackStack(null)
                            commit()
                        }
                    } else {
                        findViewById<FrameLayout>(R.id.fragmentContainer).visibility = View.VISIBLE
                        with(transaction) {
                            replace(R.id.list_frag, listFragment)
                            addToBackStack(null)
                            commit()
                        }
                    }
                }

                true
            }
            R.id.addAsunsInDB -> {
                val intent = Intent(
                    this,
                    AddActivity::class.java
                )
                startActivity(intent)

                true
            }
            R.id.openProfile -> {
                if (auth.currentUser != null) {
                    if (getCurFragment() != "profile") {
                        openProfile()
                    }
                } else {
                    val intent = Intent(
                        this,
                        SplashActivity::class.java
                    )
                    intent.putExtra("auth", true)
                    startActivity(intent)
                    finish()
                }

                true
            }
            R.id.signout -> {
                Firebase.auth.signOut()
                mGoogleSignInClient.revokeAccess()
                val intent = intent
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        if (count == 1) supportActionBar?.setDisplayHomeAsUpEnabled(false)

        if (getCurFragment() == "profile" && container.tag != "usual_display") {
            findViewById<FrameLayout>(R.id.fragmentContainer).visibility = View.VISIBLE
        }

        Log.d("c", count.toString())

        super.onBackPressed()
    }

    private fun openProfile() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (auth.currentUser != null) {
            val listFragment = ProfileFragment()
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            if (container.tag == "usual_display") {
                with (transaction) {
                    replace(R.id.fragmentContainer, listFragment)
                    addToBackStack(null)
                    commit()
                }
            } else {
                findViewById<FrameLayout>(R.id.fragmentContainer).visibility = View.GONE
                with (transaction) {
                    replace(R.id.list_frag, listFragment)
                    addToBackStack(null)
                    commit()
                }
            }
        }
    }

    private fun openMain() {
        val listFragment = AsunaListFragment()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()

        if (container.tag == "usual_display") {
            with (transaction) {
                replace(R.id.fragmentContainer, listFragment)
                commit()
            }
        } else {
            with (transaction) {
                replace(R.id.list_frag, listFragment)
                commit()
            }

            val asuna = AsunaFragment()
            asuna.setAsuna("asuna01")
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, asuna)
                .commit()

        }
    }

    fun setDisplayBack(show: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(show)
    }

    private fun showAdv() {
//        if (inter.isLoaded) {
//            inter.show()
//        } else {
//            Toast.makeText(
//                baseContext, "Failed.",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
    }

    private fun getCurFragment(): String {
        val fragment: Fragment? = if (container.tag == "usual_display") {
            supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        } else {
            supportFragmentManager.findFragmentById(R.id.list_frag)
        }

        return when (fragment) {
            is AsunaListFragment -> "main"
            is FavoriteListFragment -> "favorite"
            is ProfileFragment -> "profile"
            is AsunaFragment -> "asana"
            else -> ""
        }
    }
}


