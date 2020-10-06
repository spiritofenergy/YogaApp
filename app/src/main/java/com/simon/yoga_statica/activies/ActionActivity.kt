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


class ActionActivity : AppCompatActivity() {

    private var mainHandler = Handler(Looper.getMainLooper())
    var list: ArrayList<String> = arrayListOf()
    private lateinit var simplePlayer: MediaPlayer
    private lateinit var doublePlayer: MediaPlayer

    private lateinit var auth: FirebaseAuth

    var counter = Counter()
    private var isReady = true

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val thumbnails: StorageReference = storage.reference.child("thumbnails")

    private lateinit var advController: AdvController

    lateinit var nameAsuna: TextView
    lateinit var imageMain: ImageView
    lateinit var time: TextView
    lateinit var cong: TextView
    lateinit var allTime: TextView
    private lateinit var pauseAction: Button

    var x: Int = 0

    private val IS_READY = "isReady"
    private val CURRENT_SECOND = "currentSecond"
    private val CURRENT_POSITION = "currentPosition"
    private val LIST = "list"

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"
    private val APP_PREFERENCES_COUNT = "count"
    private val APP_PREFERENCES_FRAGMENT = "fragment"

    private var finish: Boolean = false

    private lateinit var inter: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle("Тренировка")

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

        if (savedInstanceState != null) {
            with(savedInstanceState) {
                isReady = getBoolean(IS_READY)
                counter.sec = getInt(CURRENT_SECOND)
                counter.position = getInt(CURRENT_POSITION)

                list = getStringArrayList(LIST) as ArrayList<String>

                addAsuna(list[counter.position])
            }
        } else {
            list = intent.getStringArrayListExtra("list") as ArrayList<String>
        }

        advController = AdvController(this)
        advController.init()

        inter = advController.createInterstitialAds(R.string.ads_inter_uid)

        simplePlayer = MediaPlayer.create(this, R.raw.beeps_m)
        doublePlayer = MediaPlayer.create(this, R.raw.beeps)

        nameAsuna = findViewById(R.id.nameAsuna)
        imageMain = findViewById(R.id.image2)
        time = findViewById(R.id.time)
        cong = findViewById(R.id.cong)
        allTime = findViewById(R.id.allTime)
        pauseAction = findViewById(R.id.pauseAction)

        x = if (!prefs.contains(APP_PREFERENCES_COUNT)) {
            30
        } else {
            when (prefs.getInt(APP_PREFERENCES_COUNT, 30)) {
                30 -> 30
                60 -> 60
                90 -> 90
                else -> 30
            }
        }

        Log.d("sec", x.toString())

        Thread {
            mainHandler.post(updateTextTask)
        }.start()

        Log.d("list", list.toString())

        pauseAction.setOnClickListener {
            if (it.tag == "active") {
                mainHandler.removeCallbacks(updateTextTask)
                it.tag = "pause"
                pauseAction.text = "Старт"
            } else {
                mainHandler.post(updateTextTask)
                it.tag = "active"
                pauseAction.text = "Пауза"
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTextTask)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putString(APP_PREFERENCES_FRAGMENT, "action")
            putBoolean(IS_READY, isReady)
            putInt(CURRENT_SECOND, counter.sec)
            putInt(CURRENT_POSITION, counter.position)
            putStringArrayList(LIST, list)
        }
    }

    private val updateTextTask = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            if (counter.position < list.size) {
                counter.sec -= 1

                if (isReady) {
                    addAsuna(list[counter.position])
                    nameAsuna.visibility = View.GONE
                    imageMain.visibility = View.GONE
                    findViewById<TextView>(R.id.textActAsuna).text = "Приготовьтесь"

                    if (counter.sec == 9) {
                        Thread.sleep(1500)
                    }

                    if (counter.sec == 3 || counter.sec == 6) {
                        simplePlayer.start()
                    }
                    if (counter.sec == 0) {
                        doublePlayer.start()
                    }

                } else {
                    nameAsuna.visibility = View.VISIBLE
                    imageMain.visibility = View.VISIBLE

                    findViewById<TextView>(R.id.textActAsuna).text = "Оставшееся время"
                }

                if (counter.sec >= 10) {
                    time.text = "00:${counter.sec}"
                } else {
                    time.text = "00:0${counter.sec}"
                }


                if (counter.sec == 0 && isReady) {
                    counter.sec = x
                    isReady = false
                }
                if (counter.sec == 0 && !isReady) {
                    counter.position += 1
                    counter.sec = 11
                    isReady = true

                    db.collection("users")
                        .whereEqualTo("id", auth.currentUser?.uid)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                for (document in documents) {

                                    val countAsuns = (document["countAsuns"] as Long).toInt()

                                    db.collection("users")
                                        .document(document.id)
                                        .update("countAsuns", countAsuns + 1)

                                }


                            }

                        }
                        .addOnFailureListener { exception ->
                            Log.w("home", "Error getting documents: ", exception)
                        }

                }

                mainHandler.postDelayed(this, 1000)
            } else {
                Thread.sleep(1500)
                nameAsuna.visibility = View.GONE
                imageMain.visibility = View.GONE
                time.visibility = View.GONE
                pauseAction.visibility = View.GONE
                findViewById<TextView>(R.id.textActAsuna).visibility = View.GONE
                cong.visibility = View.VISIBLE
                allTime.visibility = View.VISIBLE
                allTime.text = "Общее время тренировки: ${(x.toFloat() * list.size) / 60f} мин."
                mainHandler.removeCallbacks(this)

                supportActionBar?.setDisplayHomeAsUpEnabled(true)

                finish = true
            }
        }
    }

    private fun addAsuna(asuna: String) {

        db.collection("asunaRU").document(asuna)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    nameAsuna.text = document.data?.get("title").toString()

                    thumbnails.child("${document.data?.get("thumbPath")}.jpeg")
                        .downloadUrl
                        .addOnSuccessListener {

                            Glide.with(this)
                                .load(it)
                                .into(imageMain)

                        }.addOnFailureListener { exception ->
                            Log.d("log", "get failed with ", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("gets", "Error getting documents.", exception)
            }
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



    override fun onBackPressed() {
        mainHandler.removeCallbacks(updateTextTask)

        if (!finish) {
            AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Завершение тренировки")
                .setMessage("Вы уверены, что хотите завершить тренировку?")
                .setPositiveButton("Завершить") { dialog, which ->
                    showAdv()
                    super.onBackPressed()
                }
                .setNegativeButton("Нет") { dialogInterface: DialogInterface, i: Int ->
                    mainHandler.post(updateTextTask)
                }
                .show()
        } else {
            showAdv()
            super.onBackPressed()
        }
    }

    private fun showAdv() {
        if (inter.isLoaded) {
            inter.show()
        }
    }
}