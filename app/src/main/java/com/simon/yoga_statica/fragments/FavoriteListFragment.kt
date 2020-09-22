package com.simon.yoga_statica.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.activies.ActionActivity
import com.simon.yoga_statica.adapters.CardAdapter
import com.simon.yoga_statica.classes.Card
import com.simon.yoga_statica.interfaces.OnRecyclerItemClickListener

class FavoriteListFragment : Fragment() {

    private lateinit var asunaFavList: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var addAll: Button
    private val db = Firebase.firestore

    private var cardsArr = mutableListOf<Card>()
    private var allAsuna = mutableListOf<String>()
    private var addsAsuna = mutableListOf<String>()

    private lateinit var auth: FirebaseAuth

    private var id: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_favorite, container, false)

        auth = Firebase.auth

        id = auth.currentUser?.uid

        if (id.isNullOrEmpty()) {
            id = "null"
        }

        asunaFavList = rootView.findViewById(R.id.asunaFavList)
        fab = rootView.findViewById(R.id.floatingActionButton4)
        addAll = rootView.findViewById(R.id.addAll)

        addAll.setOnClickListener {
            addsAsuna.addAll(allAsuna)
            addsAsuna = addsAsuna.distinct() as MutableList<String>
            addsAsuna.sortBy { it }
            Log.d("list", addsAsuna.toString())

            fab.visibility = View.VISIBLE

            Toast.makeText(
                activity, "Все асуны добавлены в список выполняемых асун",
                Toast.LENGTH_SHORT
            ).show()
        }

        fab.setOnClickListener {
            val actionFragment = ActionFragment()
            actionFragment.setListAsuns(ArrayList(addsAsuna))
            val transaction: FragmentTransaction? = fragmentManager?.beginTransaction()
            transaction?.replace(R.id.fragmentContainer, actionFragment)
            transaction?.addToBackStack(null)
            transaction?.commit()

            addsAsuna.clear()
            fab.visibility = View.GONE
        }

        return rootView
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
                            val cardAdapter = fragmentManager?.let { CardAdapter(cardsArr, it) }
                            cardAdapter?.setOnDeleteListener(object : OnRecyclerItemClickListener {
                                override fun onItemClicked(asuna: String, position: Int) {
                                    if (asuna in addsAsuna) {
                                        addsAsuna.removeAt(addsAsuna.indexOf(asuna))
                                        Log.d("list", addsAsuna.toString())
                                        Toast.makeText(
                                            activity, "Асуна удалена из списка выполняемых асун",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        addsAsuna.add(asuna)
                                        addsAsuna.sortBy { it }
                                        Log.d("list", addsAsuna.toString())
                                        Toast.makeText(
                                            activity, "Асуна добавлена в список выполняемых асун",
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
                                layoutManager = LinearLayoutManager(activity)
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
}