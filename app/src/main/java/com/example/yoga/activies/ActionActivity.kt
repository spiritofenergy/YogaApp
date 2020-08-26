package com.example.yoga.activies

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.storage.ktx.storage
import com.example.yoga.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import org.w3c.dom.Text

class ActionActivity : AppCompatActivity() {
    private lateinit var receivedIntent: Intent
    lateinit var mainHandler: Handler
    lateinit var list: ArrayList<String>

    var position = 0
    var x = 30

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val thumbnails: StorageReference = storage.reference.child("thumbnails")
    lateinit var nameAsuna: TextView
    lateinit var imageMain: ImageView
    lateinit var time: TextView
    lateinit var cong: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        receivedIntent = intent
        list = receivedIntent.getIntegerArrayListExtra("listAsuna") as ArrayList<String>

        nameAsuna = findViewById(R.id.nameAsuna)
        imageMain = findViewById(R.id.image2)
        time = findViewById(R.id.time)
        cong = findViewById(R.id.cong)

        mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(updateTextTask)

        Log.d("list", list.toString())
    }

    private val updateTextTask = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            mainHandler.removeCallbacks(second)
            if (position < list.size) {
                db.collection("asunaRU").document(list[position])
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            nameAsuna.text = document.data?.get("title").toString()

                            thumbnails.child("${document.data?.get("thumbPath")}.jpeg")
                                .downloadUrl
                                .addOnSuccessListener {
                                    Glide.with(this@ActionActivity)
                                        .load(it)
                                        .into(imageMain)
                                }.addOnFailureListener { exception ->
                                    Log.d("log", "get failed with ", exception)
                                }
                            x = 30
                            mainHandler.post(second)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("gets", "Error getting documents.", exception)
                    }
                position += 1
                Log.d("pos", position.toString())
            } else {
                nameAsuna.visibility = View.GONE
                imageMain.visibility = View.GONE
                time.visibility = View.GONE
                findViewById<TextView>(R.id.textView3).visibility = View.GONE
                cong.visibility = View.VISIBLE
            }
            mainHandler.postDelayed(this, 32000)


        }
    }

    private val second = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {

            time.text = "00:${
                if (x >= 10) {
                    x
                } else {
                    "0${x}"
                }
            }"

            x -= 1
            if (x < 0) {
                x = 0
            }

            mainHandler.postDelayed(this, 1000)
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
}