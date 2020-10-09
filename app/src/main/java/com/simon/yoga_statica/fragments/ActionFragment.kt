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
import com.simon.yoga_statica.classes.Counter
import kotlinx.android.synthetic.main.fragment_action.view.*

class ActionFragment : Fragment() {

    var list: ArrayList<String> = arrayListOf()
    private lateinit var simplePlayer: MediaPlayer
    private lateinit var doublePlayer: MediaPlayer

    private lateinit var auth: FirebaseAuth
    private var isReady = true

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val thumbnails: StorageReference = storage.reference.child("thumbnails")

    lateinit var nameAsuna: TextView
    lateinit var textAsana: TextView
    lateinit var imageMain: ImageView
    lateinit var time: Chronometer
    lateinit var timeCur: Chronometer

    lateinit var startPauseAction: Button

    var x: Int = 0
    var position: Int = 0

    private val IS_READY = "isReady"
    private val CURRENT_SECOND = "currentSecond"
    private val CURRENT_POSITION = "currentPosition"
    private val LIST = "list"

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_COUNT = "count"
    private val APP_PREFERENCES_FRAGMENT = "fragment"

    private lateinit var rootView: View

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_action, container, false)

        prefs = activity?.getSharedPreferences("settings", Context.MODE_PRIVATE)!!

        if (savedInstanceState != null) {
            with(savedInstanceState) {
                isReady = getBoolean(IS_READY)
                position = getInt(CURRENT_POSITION)

                list = getStringArrayList(LIST) as ArrayList<String>

                addAsuna(list[position])
            }
        }

        auth = Firebase.auth

        simplePlayer = MediaPlayer.create(activity, R.raw.beeps_m)
        doublePlayer = MediaPlayer.create(activity, R.raw.beeps)

        nameAsuna = rootView.findViewById(R.id.nameAsuna)
        textAsana = rootView.descAsana
        imageMain = rootView.findViewById(R.id.image2)
        time = rootView.timeAllAction
        timeCur = rootView.timeCur
        startPauseAction = rootView.startPauseAction

        timeCur

        time.base = SystemClock.elapsedRealtime()
        time.start()

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

        addAsuna(list[position])

        timeCur.isCountDown = true
        timeCur.base = SystemClock.elapsedRealtime() + 1000 * x

        timeCur.onChronometerTickListener = Chronometer.OnChronometerTickListener {
            val sec = SystemClock.elapsedRealtime() - it.base
            Log.d("hello", sec.toString())

            if (sec > 0) {

                timeCur.stop()
                timeCur.base = SystemClock.elapsedRealtime() + 1000 * x
                position += 1
                if (position < list.size)
                    addAsuna(list[position])
                else
                    openCongratulations()
            }
        }

        startPauseAction.setOnClickListener {
            timeCur.base = SystemClock.elapsedRealtime() + 1000 * x
            timeCur.start()
        }

        return rootView
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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putString(APP_PREFERENCES_FRAGMENT, "action")
//            putInt(CURRENT_SECOND, counter.sec)
            putInt(CURRENT_POSITION, position)
            putStringArrayList(LIST, list)
        }
    }

    private fun openCongratulations() {
        val fragment = CongratulationFragment()
        val transaction: FragmentTransaction? = fragmentManager?.beginTransaction()
        if (transaction != null)
            with (transaction) {
                replace(R.id.frame_action, fragment)
                commit()
            }
    }
}