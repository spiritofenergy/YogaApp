package com.simon.yoga_statica.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.simon.yoga_statica.R
import com.simon.yoga_statica.classes.Counter

class ActionFragment : Fragment() {

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
    lateinit var nameAsuna: TextView
    lateinit var imageMain: ImageView
    lateinit var time: TextView
    lateinit var cong: TextView
    lateinit var allTime: TextView
    var x: Int = 0

    private val IS_READY = "isReady"
    private val CURRENT_SECOND = "currentSecond"
    private val CURRENT_POSITION = "currentPosition"
    private val LIST = "list"

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_COUNT = "count"
    private val APP_PREFERENCES_FRAGMENT = "fragment"

    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_action, container, false)

        prefs = activity?.getSharedPreferences("settings", Context.MODE_PRIVATE)!!

        if (savedInstanceState != null) {
            with (savedInstanceState) {
                isReady = getBoolean(IS_READY)
                counter.sec = getInt(CURRENT_SECOND)
                counter.position = getInt(CURRENT_POSITION)

                list = getStringArrayList(LIST) as ArrayList<String>

                addAsuna(list[counter.position])
            }
        }

        auth = Firebase.auth

        simplePlayer = MediaPlayer.create(activity, R.raw.beeps_m)
        doublePlayer = MediaPlayer.create(activity, R.raw.beeps)

        nameAsuna = rootView.findViewById(R.id.nameAsuna)
        imageMain = rootView.findViewById(R.id.image2)
        time = rootView.findViewById(R.id.time)
        cong = rootView.findViewById(R.id.cong)
        allTime = rootView.findViewById(R.id.allTime)

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


        return rootView
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
                    rootView.findViewById<TextView>(R.id.textActAsuna).text = "Приготовьтесь"

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

                    rootView.findViewById<TextView>(R.id.textActAsuna).text = "Оставшееся время"
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

                mainHandler.postDelayed(this,1000)
            } else {
                Thread.sleep(1500)
                nameAsuna.visibility = View.GONE
                imageMain.visibility = View.GONE
                time.visibility = View.GONE
                rootView.findViewById<TextView>(R.id.textActAsuna).visibility = View.GONE
                cong.visibility = View.VISIBLE
                allTime.visibility = View.VISIBLE
                allTime.text = "Общее время тренировки: ${(x.toFloat() * list.size) / 60f} мин."
                mainHandler.removeCallbacks(this)
            }
        }
    }

    fun setListAsuns(listR: ArrayList<String>) {
        list = listR
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
                            if (isAdded) {
                                Glide.with(this)
                                    .load(it)
                                    .into(imageMain)
                            }
                        }.addOnFailureListener { exception ->
                            Log.d("log", "get failed with ", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("gets", "Error getting documents.", exception)
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
}