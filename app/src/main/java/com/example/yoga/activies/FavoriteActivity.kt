package com.example.yoga.activies

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yoga.R
import com.example.yoga.adapters.CardAdapter
import com.example.yoga.classes.Card
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class FavoriteActivity : AppCompatActivity() {

    private lateinit var asunaFavList: RecyclerView
    private val db = Firebase.firestore

    private var cardsArr = mutableListOf<Card>()
    private lateinit var androidID: String

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = "Любимые Асуны"

        asunaFavList = findViewById(R.id.asunaFavList)

        androidID = Settings.Secure.getString(
            this.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        getList()
    }

    private fun getList() {
        db.collection("asunaRU")
            .get()
            .addOnSuccessListener { result ->
                cardsArr.clear()
                for (document in result) {
                    val card = Card()
                    card.id = document.id
                    db.collection("likes").document(card.id)
                        .get()
                        .addOnSuccessListener { asuna ->
                            if (asuna != null) {
                                if (asuna.contains(androidID)) {
                                    if (asuna.data?.get(androidID) as Boolean) {
                                        card.title = document.data["title"].toString()
                                        card.likesCount = (document.data["likes"] as Long).toInt()
                                        card.commentsCount = (document.data["comments"] as Long).toInt()
                                        card.thumbPath = document.data["thumbPath"].toString()
                                        cardsArr.add(card)
                                    }
                                }
                            }
                            asunaFavList.apply {
                                layoutManager = LinearLayoutManager(this@FavoriteActivity)
                                adapter = CardAdapter(cardsArr)
                            }
                        }
                        .addOnFailureListener { exception ->
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