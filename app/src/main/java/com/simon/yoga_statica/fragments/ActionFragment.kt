package com.simon.yoga_statica.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.icu.text.MessageFormat.format
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.text.format.DateFormat.format
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.simon.yoga_statica.R
import kotlinx.android.synthetic.main.fragment_action.*
import kotlinx.android.synthetic.main.fragment_action.view.*
import org.w3c.dom.Document
import java.lang.Math.abs
import java.lang.String.format
import java.text.DateFormat
import java.text.MessageFormat

class ActionFragment : Fragment() {

    var list: ArrayList<String> = arrayListOf()
    private lateinit var simplePlayer: MediaPlayer
    private lateinit var doublePlayer: MediaPlayer

    private lateinit var auth: FirebaseAuth

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val thumbnails: StorageReference = storage.reference.child("thumbnails")

    private lateinit var actionBar: ProgressBar
    private lateinit var actionBarAll: ProgressBar

    lateinit var nameAsuna: TextView
    lateinit var textAsana: TextView
    private lateinit var partAction: TextView
    lateinit var imageMain: ImageView
    lateinit var time: Chronometer
    lateinit var timeCur: Chronometer

    lateinit var startPauseAction: Button

    var x: Int = 0
    var position: Int = 0
    var countAll = 0

    var curSec: Int = 0
    var allSec: Int = 0

    var isStart = false
    var closeExist = true
    var dyhExist = ""
    var isDyh = false

    private var allTimeSec = 0

    private val IS_START = "isStart"
    private val CURRENT_SECOND = "currentSecond"
    private val CURRENT_POSITION = "currentPosition"
    private val LIST = "list"
    private val ALL_SECOND = "allSec"

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_COUNT = "count"
    private val APP_PREFERENCES_FRAGMENT = "fragment"
    private val APP_PREFERENCES_SHAVA = "shava"
    private val APP_PREFERENCES_DYH = "dyh"
    private val APP_PREFERENCES_THEME = "theme"

    private lateinit var rootView: View

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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

        auth = Firebase.auth

        val theme = if (!prefs.contains(APP_PREFERENCES_THEME) || auth.currentUser == null) {
            "default"
        } else {
            when (prefs.getString(APP_PREFERENCES_THEME, "default")) {

                "default" -> "default"
                "red"    -> "red"
                "orange" -> "orange"
                "lime"  -> "lime"
                "coffee" -> "coffee"
                else     -> "default"
            }
        }

        Log.d("all", allSec.toString())
        Log.d("is", isStart.toString())
        Log.d("curSec", curSec.toString())
        Log.d("list", list.toString())

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

        actionBar = rootView.actionBar
        actionBarAll = rootView.actionBarAll

        actionBar.progressDrawable = resources.getDrawable(resources.getIdentifier(
            "action_bar_$theme",
            "drawable",
            context?.packageName
        ), context?.theme)
        actionBarAll.progressDrawable = resources.getDrawable(resources.getIdentifier(
            "action_bar_$theme",
            "drawable",
            context?.packageName
        ), context?.theme)

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

        dyhExist = if (!prefs.contains(APP_PREFERENCES_DYH)) {
            "dyh1"
        } else {
            prefs.getString(APP_PREFERENCES_DYH, "dyh1").toString()
        }

        closeExist = if (!prefs.contains(APP_PREFERENCES_SHAVA)) {
            true
        } else {
            prefs.getBoolean(APP_PREFERENCES_SHAVA, true)
        }

        if (dyhExist != "" || dyhExist != "off") {
            isDyh = true
            list.add(0, dyhExist)
            partAction.text = "Дыхание"
        }

        countAll = list.size
        if (closeExist) countAll++

        allTimeSec = countAll * x

        time.onChronometerTickListener = Chronometer.OnChronometerTickListener {
            val sec = SystemClock.elapsedRealtime() - it.base

            setProgress(actionBarAll, sec / 1000, allTimeSec, true)
        }

        timeCur.onChronometerTickListener = Chronometer.OnChronometerTickListener {
            val sec = SystemClock.elapsedRealtime() - it.base
            Log.d("q", kotlin.math.abs(sec / 1000).toString())
            setProgress(actionBar, kotlin.math.abs(sec / 1000), x)

            Log.d("1", it.text.split(":")[1])
            if (sec > 0) {
                timeCur.base = SystemClock.elapsedRealtime() + 1000 * x
                position += 1
                if (position < list.size) {
                    if (isDyh) {
                        isDyh = false
                        partAction.text = "Основное"
                        setUpFadeAnimation(partAction)
                    }
                    addAsuna(list[position])
                } else {
                    if (!closeExist) {
                        time.stop()
                        timeCur.stop()
                        openCongratulations()
                    } else {
                        closeExist = false
                        partAction.text = "Заключение"
                        setUpFadeAnimation(partAction)
                        addAsuna(activity?.resources?.getString(R.string.shava_id)!!)
                    }
                }
            }
        }

        startPauseAction.setOnClickListener {
            if (startPauseAction.tag == "pause") {
                if (curSec != 0) {
                    timeCur.base = SystemClock.elapsedRealtime() + 1000 * curSec
                    setProgress(actionBar, curSec.toLong(), x)
                } else {
                    timeCur.base = SystemClock.elapsedRealtime() + 1000 * x + 300
                    setProgress(actionBar, x.toLong(), x)
                }
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
        timeCur.base = SystemClock.elapsedRealtime() + 1000 * x + 300

        setProgress(actionBar, x.toLong(), x)

        if (curSec != 0) {
            timeCur.base = SystemClock.elapsedRealtime() + 1000 * curSec
            setProgress(actionBar ,curSec.toLong(), x)
            if (isStart)
                timeCur.start()
        }
    }

    fun setListAsuns(listR: ArrayList<String>) {
        list = listR
    }

    private fun addAsuna(asuna: String) {
        val documentRef: DocumentReference = if (asuna.contains("open")) {
            db.collection("openAsunaRU").document(asuna)
        } else {
            if (asuna.contains("dyh")) {
                db.collection("dyh").document(asuna)
            } else {
                db.collection("asunaRU").document(asuna)
            }
        }
        documentRef
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    nameAsuna.text = document.data?.get("title").toString()
                    if (document.data?.get("description").toString() != "null")
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
        fragment.setCount(countAll)
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

            override fun onAnimationRepeat(arg: Animation) {}
            override fun onAnimationStart(arg: Animation) {}
        })

        textView.startAnimation(fadeIn)
    }

    private fun setProgress(actionBar: ProgressBar, sec: Long, limit: Int, notInverce: Boolean = false) {
        var progress = (((sec * 100) / limit).toInt())
        if (!notInverce) {
            progress = 100 - progress
        }
        actionBar.progress = progress
    }
}