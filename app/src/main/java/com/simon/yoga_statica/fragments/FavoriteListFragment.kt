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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.activies.ActionActivity
import com.simon.yoga_statica.adapters.CardAdapter
import com.simon.yoga_statica.classes.AdvController
import com.simon.yoga_statica.classes.Card
import com.simon.yoga_statica.interfaces.OnRecyclerItemClickListener

class FavoriteListFragment : Fragment() {

    private lateinit var asunaFavList: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var addAll: Button
    private val db = Firebase.firestore

    private var count = 0

    private var cardsArr = mutableListOf<Card>()
    private var allAsuna = mutableListOf<String>()
    private var addsAsuna = mutableListOf<String>()

    private lateinit var auth: FirebaseAuth

    private lateinit var advController: AdvController
    private lateinit var inter: InterstitialAd

    private var id: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_favorite, container, false)

        activity?.title = getString(R.string.favorite_asana)

        auth = Firebase.auth

        id = auth.currentUser?.uid

        if (id.isNullOrEmpty()) {
            id = "null"
        }

        advController = AdvController(container?.context!!)
        advController.init()

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
                activity, getString(R.string.all_additional),
                Toast.LENGTH_SHORT
            ).show()
        }

        fab.setOnClickListener {
            showAds()
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        getList()

        inter = advController.createInterstitialAds(R.string.ads_inter_uid)

        inter.adListener = object: AdListener() {
            override fun onAdClosed() {
                val intent = Intent(
                    activity,
                    ActionActivity::class.java
                )

                intent.putExtra("list", ArrayList(addsAsuna))
                addsAsuna.clear()
                startActivity(intent)

                addsAsuna.clear()
                fab.visibility = View.GONE
            }
        }
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
                                        card.shortDesc = document.data["shortDescription"].toString()
                                        cardsArr.add(card)
                                        card.openAsans = document.data["openAsans"].toString()
                                        allAsuna.addAll(card.openAsans.split(" "))
                                        allAsuna.add(card.id)
                                    }
                                }
                            }
                            val cardAdapter = fragmentManager?.let { CardAdapter(cardsArr, it) }
                            cardAdapter?.setOnClickAdd(object : OnRecyclerItemClickListener {
                                override fun onItemClicked(position: Int, asuna: String) {
                                    var isExist = false
                                    val asunaList = asuna.split(" ")
                                    for (asunaOne in asunaList) {
                                        if (asunaOne in addsAsuna) {
                                            isExist = true
                                        }
                                    }
                                    if (isExist) {
                                        for (asunaOne in asunaList) {
                                            addsAsuna.removeAt(addsAsuna.indexOf(asunaOne))
                                        }
                                        Log.d("list", addsAsuna.toString())
                                        Toast.makeText(
                                            activity, getString(R.string.delete_asana),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        addsAsuna.addAll(asunaList)

                                        Log.d("list", addsAsuna.toString())
                                        Toast.makeText(
                                            activity, getString(R.string.asuna_addition),
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

                            cardAdapter?.setOnClickOpen(object : OnRecyclerItemClickListener {
                                override fun onItemClicked(position: Int, asuna: String) {
                                    val listFragment = AsunaFragment()
                                    listFragment.setAsuna(asuna)
                                    fragmentManager?.beginTransaction()
                                        ?.replace(R.id.fragmentContainer, listFragment)
                                        ?.addToBackStack(null)
                                        ?.commit()
                                }

                                override fun onItemLongClicked(position: Int) {
                                }

                            })

                            cardAdapter?.cardCount = cardsArr.size

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

    private fun showAds() {
        if (inter.isLoaded) {
            inter.show()
        } else {

            val intent = Intent(
                activity,
                ActionActivity::class.java
            )

            intent.putExtra("list", ArrayList(addsAsuna))
            addsAsuna.clear()
            startActivity(intent)

            fab.visibility = View.GONE

        }
    }
}