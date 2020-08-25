package com.example.yoga.activies

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yoga.R
import com.example.yoga.adapters.CardAdapter
import com.example.yoga.classes.Card
import com.example.yoga.interfaces.OnRecyclerItemClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var cardsRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private val db = Firebase.firestore

    private var cardsArr = mutableListOf<Card>()
    private var addsAsuna = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("lang", Locale.getDefault().displayLanguage)

        cardsRecyclerView = findViewById(R.id.cards)
        fab = findViewById(R.id.floatingActionButton3)

        fab.setOnClickListener {
            val intent = Intent(
                this,
                ActionActivity::class.java
            )

            intent.putExtra("listAsuna", ArrayList(addsAsuna))
            addsAsuna.clear()
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
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
                    card.title = document.data["title"].toString()
                    card.likesCount = (document.data["likes"] as Long).toInt()
                    card.commentsCount = (document.data["comments"] as Long).toInt()
                    card.thumbPath = document.data["thumbPath"].toString()
                    cardsArr.add(card)
                }

                val cardAdapter = CardAdapter(cardsArr)
                cardAdapter.setOnDeleteListener( object : OnRecyclerItemClickListener {
                    override fun onItemClicked(asuna: String, position: Int) {
                        if (asuna in addsAsuna) {
                            addsAsuna.removeAt(addsAsuna.indexOf(asuna))
                            Log.d("list", addsAsuna.toString())
                            Toast.makeText(baseContext, "Асуна удалена из списка выполняемых асун",
                                Toast.LENGTH_LONG).show()
                        } else {
                            addsAsuna.add(asuna)
                            addsAsuna.sortBy { it }
                            Log.d("list", addsAsuna.toString())
                            Toast.makeText(baseContext, "Асуна добавлена в список выполняемых асун",
                                Toast.LENGTH_LONG).show()
                        }
                        if (addsAsuna.size > 0) {
                            fab.visibility = View.VISIBLE
                        } else {
                            fab.visibility = View.GONE
                        }
                    }

                    override fun onItemLongClicked(position: Int) {

                    }

                })

                cardsRecyclerView.apply {
                    layoutManager = LinearLayoutManager(this@MainActivity)
                    adapter = cardAdapter
                }
            }
            .addOnFailureListener { exception ->
                Log.w("gets", "Error getting documents.", exception)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.favoriteBut -> {
                val intent = Intent(
                    this,
                    FavoriteActivity::class.java
                )
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }
}