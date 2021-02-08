package com.simon.yoga_statica.activies

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.classes.AdvController
import com.simon.yoga_statica.fragments.ActionFragment

/**
 * Класс активити выполнения тренировки
 *
 */
class ActionActivity : AppCompatActivity() {
    var list: ArrayList<String> = arrayListOf()

    private lateinit var auth: FirebaseAuth

    private lateinit var advController: AdvController

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"

    var finish: Boolean = false

    private lateinit var inter: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = getString(R.string.workout_title)

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

        setContentView(R.layout.activity_action)

        list = intent.getStringArrayListExtra("list") as ArrayList<String>

        advController = AdvController(this)
        advController.init()

        inter = advController.createInterstitialAds(R.string.ads_inter_uid)

        if (savedInstanceState == null)
            openAsanaAct()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openAsanaAct() {
        val listFragment = ActionFragment()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        listFragment.setListAsuns(list)
        with (transaction) {
            replace(R.id.frame_action, listFragment)
            commit()
        }
    }

    override fun onBackPressed() {
        if (!finish) {
            val fragment = supportFragmentManager.findFragmentById(R.id.frame_action)
            fragment as ActionFragment
            fragment.onPause()

            AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.end_workout))
                .setMessage(getString(R.string.confirm_workout_end))
                .setPositiveButton(getString(R.string.ending)) { _, _ ->
                    showAdv()
                    super.onBackPressed()
                }
                .setNegativeButton(getString(R.string.no)) { _: DialogInterface, _: Int ->
                    fragment.onStart()
                }
                .show()
        } else {
            showAdv()
            super.onBackPressed()
        }
    }

    fun setDisplayBack(show: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(show)
    }

    private fun showAdv() {
//        if (inter.isLoaded) {
//            inter.show()
//        }
    }
}