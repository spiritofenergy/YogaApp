package com.simon.yoga_statica.fragments

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.simon.yoga_statica.R
import kotlinx.android.synthetic.main.fragment_action.view.*


class ActionFragment : Fragment() {

    var list: ArrayList<String> = arrayListOf()
    private lateinit var simplePlayer: MediaPlayer
    private lateinit var doublePlayer: MediaPlayer

    private lateinit var auth: FirebaseAuth

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val thumbnails: StorageReference = storage.reference.child("thumbnails")

    lateinit var nameAsuna: TextView
    lateinit var textAsana: TextView
    private lateinit var partAction: TextView
    lateinit var imageMain: ImageView
    lateinit var time: Chronometer
    lateinit var timeCur: Chronometer

    lateinit var startPauseAction: Button

    var x: Int = 0
    var position: Int = 0

    var curSec: Int = 0
    var allSec: Int = 0

    var isStart = false

    private val IS_START = "isStart"
    private val CURRENT_SECOND = "currentSecond"
    private val CURRENT_POSITION = "currentPosition"
    private val LIST = "list"
    private val ALL_SECOND = "allSec"

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
            with(savedInstanceState) {
                isStart = getBoolean(IS_START)
                position = getInt(CURRENT_POSITION)
                curSec = getInt(CURRENT_SECOND)
                allSec = getInt(ALL_SECOND)
                list = getStringArrayList(LIST) as ArrayList<String>

                addAsuna(list[position])
            }
        }

        Log.d("all", allSec.toString())
        Log.d("is", isStart.toString())
        Log.d("curSec", curSec.toString())
        Log.d("list", list.toString())

        auth = Firebase.auth

        simplePlayer = MediaPlayer.create(activity, R.raw.beeps_m)
        doublePlayer = MediaPlayer.create(activity, R.raw.beeps)

        partAction = rootView.partAction

        partAction.text = "Основное"

        nameAsuna = rootView.findViewById(R.id.nameAsuna)
        textAsana = rootView.descAsana
        imageMain = rootView.findViewById(R.id.image2)
        time = rootView.timeAllAction
        timeCur = rootView.timeCur
        startPauseAction = rootView.startPauseAction

        if (isStart) {
            startPauseAction.tag = "active"
            startPauseAction.text = activity?.resources?.getString(R.string.stop_action)
        }

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

        timeCur.onChronometerTickListener = Chronometer.OnChronometerTickListener {
            val sec = SystemClock.elapsedRealtime() - it.base

            if (sec > 0) {

//                startPauseAction.tag = "pause"
//                startPauseAction.text = activity?.resources?.getString(R.string.start_action)
//                curSec = 0
//                isStart = false

                timeCur.base = SystemClock.elapsedRealtime() + 1000 * x
                position += 1
                if (position < list.size)
                    addAsuna(list[position])
                else {
                    time.stop()
                    timeCur.stop()
                    openCongratulations()
                }
            }
        }

        startPauseAction.setOnClickListener {
            if (startPauseAction.tag == "pause") {
                if (curSec != 0)
                    timeCur.base = SystemClock.elapsedRealtime() + 1000 * curSec
                else
                    timeCur.base = SystemClock.elapsedRealtime() + 1000 * x
                timeCur.start()

                isStart = true
                startPauseAction.tag = "active"
                startPauseAction.text = activity?.resources?.getString(R.string.stop_action)
            } else {
                timeCur.stop()
                curSec = (SystemClock.elapsedRealtime() - timeCur.base).toInt() / -1000
                startPauseAction.tag = "pause"

                isStart = false

                startPauseAction.text = activity?.resources?.getString(R.string.start_action)
            }
        }

        setUpFadeAnimation(partAction)

        addAsuna(list[position])

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()

        time.base = SystemClock.elapsedRealtime()
        if (allSec != 0)
            time.base = SystemClock.elapsedRealtime() - 1000 * allSec
        time.start()

        timeCur.isCountDown = true
        timeCur.base = SystemClock.elapsedRealtime() + 1000 * x

        if (curSec != 0) {
            timeCur.base = SystemClock.elapsedRealtime() + 1000 * curSec
            if (isStart)
                timeCur.start()
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
                    textAsana.text = document.data?.get("description").toString()

                    thumbnails.child(
                        "${
                            document.data?.get("thumbPath").toString().split(" ")[0]
                        }.jpeg"
                    )
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
        time.stop()
        timeCur.stop()
        if (isStart)
            curSec = (SystemClock.elapsedRealtime() - timeCur.base).toInt() / -1000

        allSec = (SystemClock.elapsedRealtime() - time.base).toInt() / 1000
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putString(APP_PREFERENCES_FRAGMENT, "action")
            putBoolean(IS_START, isStart)
            putInt(CURRENT_SECOND, curSec)
            putInt(ALL_SECOND, allSec)
            putInt(CURRENT_POSITION, position)
            putStringArrayList(LIST, list)
        }
    }

    private fun openCongratulations() {
        val fragment = CongratulationFragment()
        fragment.setCount(list.size)
        val transaction: FragmentTransaction? = fragmentManager?.beginTransaction()
        if (transaction != null)
            with(transaction) {
                replace(R.id.frame_action, fragment)
                commit()
            }
    }

    private fun setUpFadeAnimation(textView: TextView) {
        val fadeIn: Animation = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 1000

        val fadeOut: Animation = AlphaAnimation(1.0f, 0.0f)
        fadeOut.duration = 1000
        fadeOut.startOffset = 8000
        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(arg0: Animation) {
                textView.startAnimation(fadeOut)
            }

            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationStart(arg0: Animation) {}
        })

        textView.startAnimation(fadeIn)
    }
}