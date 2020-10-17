package com.simon.yoga_statica.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.activies.ActionActivity
import com.simon.yoga_statica.activies.MainActivity
import com.simon.yoga_statica.adapters.CardAdapter
import com.simon.yoga_statica.classes.Ad
import com.simon.yoga_statica.classes.AdUnifiedListening
import com.simon.yoga_statica.classes.AdvController
import com.simon.yoga_statica.classes.Card
import com.simon.yoga_statica.interfaces.OnRecyclerItemClickListener


class AsunaListFragment : Fragment() {
    private lateinit var cardsRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton

    private val db = Firebase.firestore
    private var cardsArr = mutableListOf<Any>()
    private var addsAsuna = mutableListOf<String>()
    private var count = 0
    private var indexAdv = 0

    private lateinit var advController: AdvController
    private lateinit var inter: InterstitialAd

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_list, container, false)

        activity?.setTitle(R.string.app_name)

        advController = AdvController(container?.context!!)
        advController.init()

        fab = rootView.findViewById(R.id.floatingActionButton3)
        cardsRecyclerView = rootView.findViewById(R.id.cards)

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

                fab.visibility = View.GONE
            }
        }
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
                    card.shortDesc = document.data["shortDescription"].toString()
                    card.openImages = document.data["openAsans"].toString().split("")
                    cardsArr.add(card)
                }
                var index = 0
                count = i

                indexAdv = count / 3

                for (counter in 0 until indexAdv) {
                    index += 3
                    val ad = Ad()
                    ad.ad = "Ad"
                    cardsArr.add(index, ad)

                    index += 1
                }

                getAdapter()
            }
            .addOnFailureListener { exception ->
                Log.w("gets", "Error getting documents.", exception)
            }

    }

    private fun getAdapter() {
        val cardAdapter = fragmentManager?.let { CardAdapter(cardsArr, it) }
        cardAdapter?.setOnClickAdd(object : OnRecyclerItemClickListener {
            override fun onItemClicked(asuna: String, position: Int) {
                if (asuna in addsAsuna) {
                    addsAsuna.removeAt(addsAsuna.indexOf(asuna))
                    Log.d("list", addsAsuna.toString())
                    Toast.makeText(
                        activity, "Асана удалена",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
//                    addsAsuna.addAll(openAsuns)
                    addsAsuna.add(asuna)
                    addsAsuna.sortBy { it }
                    Log.d("list", addsAsuna.toString())
                    Toast.makeText(
                        activity, "Асана добавлена",
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

        cardAdapter?.setOnClickOpen(object : OnRecyclerItemClickListener{
            override fun onItemClicked(asuna: String, position: Int) {
                val listFragment = AsunaFragment()
                listFragment.setAsuna(asuna)

                (activity as MainActivity).setDisplayBack(true)

                fragmentManager?.beginTransaction()
                    ?.replace(R.id.fragmentContainer, listFragment)
                    ?.addToBackStack(null)
                    ?.commit()
            }

            override fun onItemLongClicked(position: Int) {
            }

        })

        cardAdapter?.cardCount = count
        cardAdapter?.indexAdv = 3
        cardAdapter?.countAdv = indexAdv

        cardsRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = cardAdapter
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