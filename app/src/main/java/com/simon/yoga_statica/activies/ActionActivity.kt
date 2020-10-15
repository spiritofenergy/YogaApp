package com.simon.yoga_statica.activies

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.simon.yoga_statica.R
import com.simon.yoga_statica.classes.AdvController
import com.simon.yoga_statica.classes.Counter
import com.simon.yoga_statica.fragments.ActionFragment
import com.simon.yoga_statica.fragments.AsunaListFragment


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

        title = "Тренировка"

        auth = Firebase.auth

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)

        if (!prefs.contains(APP_PREFERENCES_THEME) || auth.currentUser == null) {
            setTheme(R.style.AppTheme)
        } else {
            when (prefs.getString(APP_PREFERENCES_THEME, "default")) {

                "default" -> setTheme(R.style.AppTheme)
                "red" -> setTheme(R.style.RedAppTheme)
                "orange" -> setTheme(R.style.OrangeAppTheme)
                "green" -> setTheme(R.style.GreenAppTheme)
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
                .setTitle("Завершение тренировки")
                .setMessage("Вы уверены, что хотите завершить тренировку? Статистика не сохранится!")
                .setPositiveButton("Завершить") { dialog, which ->
                    showAdv()
                    super.onBackPressed()
                }
                .setNegativeButton("Нет") { dialogInterface: DialogInterface, i: Int ->
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