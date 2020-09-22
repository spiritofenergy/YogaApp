package com.simon.yoga_statica.fragments

import android.annotation.SuppressLint
import android.content.Intent
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
    lateinit var list: ArrayList<String>
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

    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_action, container, false)

        auth = Firebase.auth

        simplePlayer = MediaPlayer.create(activity, R.raw.beeps_m)
        doublePlayer = MediaPlayer.create(activity, R.raw.beeps)

        nameAsuna = rootView.findViewById(R.id.nameAsuna)
        imageMain = rootView.findViewById(R.id.image2)
        time = rootView.findViewById(R.id.time)
        cong = rootView.findViewById(R.id.cong)
        allTime = rootView.findViewById(R.id.allTime)

        db.collection("users")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        x = (document["sec"] as Long).toInt()
                    }
                }

                if (x == 0) {
                    x = 30
                }

                Thread {
                    mainHandler.post(updateTextTask)
                }.start()
            }
            .addOnFailureListener { exception ->
                Log.w("home", "Error getting documents: ", exception)
            }



        Log.d("list", list.toString())


        return rootView
    }

    private val updateTextTask = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            if (counter.position < list.size) {
                counter.sec += 1

                if (isReady) {
                    addAsuna(list[counter.position])
                    nameAsuna.visibility = View.GONE
                    imageMain.visibility = View.GONE
                    rootView.findViewById<TextView>(R.id.textActAsuna).text = "Приготовьтесь"

                    if (counter.sec == 1) {
                        Thread.sleep(1500)
                    }

                    if (counter.sec == 3 || counter.sec == 6) {
                        simplePlayer.start()
                    }
                    if (counter.sec == 10) {
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


                if (counter.sec == 10 && isReady) {
                    counter.sec = -1
                    isReady = false
                }
                if (counter.sec == x && !isReady) {
                    counter.position += 1
                    counter.sec = -1
                    isReady = true
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
                allTime.text = "Общее время тренировки: ${(30f * list.size) / 60f} мин."
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
}