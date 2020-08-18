package com.example.yoga.activies

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.yoga.R
import com.example.yoga.adapters.CardAdapter
import com.example.yoga.classes.Card
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class AsunaActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val thumbnails: StorageReference = storage.reference.child("thumbnails")

    private lateinit var title: TextView
    private lateinit var textDescription: TextView
    private lateinit var imageWorkout: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asuna)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = findViewById(R.id.textTitle)
        textDescription = findViewById(R.id.textDescription)
        imageWorkout = findViewById(R.id.imageWorkout)


        val intent = intent
        val id = intent.getStringExtra("asunaID")

        db.collection("asunaRU").document(id.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    title.text = document.data?.get("title").toString()
                    textDescription.text = document.data?.get("description").toString()
                    thumbnails.child("${document.data?.get("thumbPath")}.jpeg")
                        .downloadUrl
                        .addOnSuccessListener {
                            Glide.with(this)
                                .load(it)
                                .into(imageWorkout)
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
}