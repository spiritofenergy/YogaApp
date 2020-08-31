package com.example.yoga.activies

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yoga.R
import com.example.yoga.adapters.CardAdapter
import com.example.yoga.classes.Card
import com.example.yoga.interfaces.OnRecyclerItemClickListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class FavoriteActivity : AppCompatActivity() {

    private lateinit var asunaFavList: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var addAll: Button
    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var gso: GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private var cardsArr = mutableListOf<Card>()
    private var allAsuna = mutableListOf<String>()
    private var addsAsuna = mutableListOf<String>()

    private var id: String? = null

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = "Любимые Асуны"

        auth = Firebase.auth

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        id = auth.currentUser?.uid

        if (id.isNullOrEmpty()) {
            id = "null"
        }

        asunaFavList = findViewById(R.id.asunaFavList)
        fab = findViewById(R.id.floatingActionButton4)
        addAll = findViewById(R.id.addAll)


        addAll.setOnClickListener {
            addsAsuna.addAll(allAsuna)
            addsAsuna = addsAsuna.distinct() as MutableList<String>
            addsAsuna.sortBy { it }
            Log.d("list", addsAsuna.toString())

            fab.visibility = View.VISIBLE

            Toast.makeText(
                baseContext, "Все асуны добавлены в список выполняемых асун",
                Toast.LENGTH_SHORT
            ).show()
        }

        fab.setOnClickListener {
            val intent = Intent(
                this,
                ActionActivity::class.java
            )

            intent.putExtra("listAsuna", ArrayList(addsAsuna))
            addsAsuna.clear()
            startActivity(intent)
            fab.visibility = View.GONE
        }

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
                                if (asuna.contains(id.toString())) {
                                    if (asuna.data?.get(id) as Boolean) {
                                        card.title = document.data["title"].toString()
                                        card.likesCount = (document.data["likes"] as Long).toInt()
                                        card.commentsCount = (document.data["comments"] as Long).toInt()
                                        card.thumbPath = document.data["thumbPath"].toString()
                                        cardsArr.add(card)
                                        allAsuna.add(card.id)
                                    }
                                }
                            }
                            val cardAdapter = CardAdapter(cardsArr)
                            cardAdapter.setOnDeleteListener(object : OnRecyclerItemClickListener {
                                override fun onItemClicked(asuna: String, position: Int) {
                                    if (asuna in addsAsuna) {
                                        addsAsuna.removeAt(addsAsuna.indexOf(asuna))
                                        Log.d("list", addsAsuna.toString())
                                        Toast.makeText(
                                            baseContext, "Асуна удалена из списка выполняемых асун",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        addsAsuna.add(asuna)
                                        addsAsuna.sortBy { it }
                                        Log.d("list", addsAsuna.toString())
                                        Toast.makeText(
                                            baseContext, "Асуна добавлена в список выполняемых асун",
                                            Toast.LENGTH_SHORT
                                        ).show()
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
                            asunaFavList.apply {
                                layoutManager = LinearLayoutManager(this@FavoriteActivity)
                                adapter = cardAdapter
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_main, menu)

        if (auth.currentUser != null) {
            menu?.getItem(3)?.isVisible = false
            menu?.getItem(0)?.isVisible = true
            menu?.getItem(1)?.isVisible = false
            menu?.getItem(2)?.isVisible = false
            menu?.getItem(4)?.isVisible = true
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.favoriteBut -> {
                val intent = Intent(
                    this,
                    FavoriteActivity::class.java
                )
                startActivity(intent)
                true
            }
            R.id.google_signin -> {
                val signInIntent: Intent = mGoogleSignInClient.signInIntent
                startActivityForResult(signInIntent, 123)

                true
            }
            R.id.signout -> {
                Firebase.auth.signOut()
                mGoogleSignInClient.revokeAccess()
                val intent = intent
                finish()
                startActivity(intent)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }
}