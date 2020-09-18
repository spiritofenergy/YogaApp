package com.simon.yoga_statica.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.adapters.CardAdapter
import com.simon.yoga_statica.classes.Card
import com.simon.yoga_statica.interfaces.OnRecyclerItemClickListener

class AsunaListFragment : Fragment() {
    private lateinit var cardsRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton

    private val db = Firebase.firestore
    private var cardsArr = mutableListOf<Card>()
    private var addsAsuna = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_list, container, false)

        fab = rootView.findViewById(R.id.floatingActionButton3)
        cardsRecyclerView = rootView.findViewById(R.id.cards)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        getList()
    }

    private fun getList() {
        var i = 0
        db.collection("asunaRU")
            .get()
            .addOnSuccessListener { result ->
                cardsArr.clear()
                for (document in result) {
                    i += 1
                    val card = Card()
                    card.id = document.id
                    card.title = document.data["title"].toString()
                    card.likesCount = (document.data["likes"] as Long).toInt()
                    card.commentsCount = (document.data["comments"] as Long).toInt()
                    card.thumbPath = document.data["thumbPath"].toString()
                    cardsArr.add(card)
                    if (i == 5) {
                        i += 1
                        val cardAdv = Card()
                        cardAdv.id = "ADV"
                        //cardsArr.add(cardAdv)
                    }
                }

                val cardAdapter = CardAdapter(cardsArr)
                cardAdapter.setOnDeleteListener(object : OnRecyclerItemClickListener {
                    override fun onItemClicked(asuna: String, position: Int) {
                        if (asuna in addsAsuna) {
                            addsAsuna.removeAt(addsAsuna.indexOf(asuna))
                            Log.d("list", addsAsuna.toString())
                            Toast.makeText(
                                activity, "Асана удалена",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            addsAsuna.add(asuna)
                            addsAsuna.sortBy { it }
                            Log.d("list", addsAsuna.toString())
                            Toast.makeText(
                                activity , "Асана добавлена",
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

                cardsRecyclerView.apply {
                    layoutManager = LinearLayoutManager(activity)
                    adapter = cardAdapter
                }
            }
            .addOnFailureListener { exception ->
                Log.w("gets", "Error getting documents.", exception)
            }

    }
}