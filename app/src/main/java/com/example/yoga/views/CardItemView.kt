package com.example.yoga.views

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yoga.R
import com.example.yoga.activies.AsunaActivity
import com.example.yoga.activies.MainActivity
import com.example.yoga.classes.Card
import com.example.yoga.interfaces.OnRecyclerItemClickListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class CardItemView(inflater: LayoutInflater, private val parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_view, parent, false)) {
    private var counterTwo: TextView = itemView.findViewById(R.id.counterTwo)
    private var counterFirst: TextView = itemView.findViewById(R.id.counterFirst)
    private var titleCard: TextView = itemView.findViewById(R.id.asanaTitle)
    private var socialAll: TextView = itemView.findViewById(R.id.socialAll)
    private var publish: TextView = itemView.findViewById(R.id.publish)
    private var layoutDate1: FrameLayout = itemView.findViewById(R.id.layoutDate1)
    private var social: FrameLayout = itemView.findViewById(R.id.social)
    private var likeImg: ImageView = itemView.findViewById(R.id.likeImg)
    private var yogaIconGrand: ImageView = itemView.findViewById(R.id.yogaIconGrand)
    private var image: ImageView = itemView.findViewById(R.id.image)
    private var buttonSettings: ImageView = itemView.findViewById(R.id.buttonSettings)

    private var asunaCard: RelativeLayout = itemView.findViewById(R.id.asunaCard)
    private var ads: RelativeLayout = itemView.findViewById(R.id.ads)

    private var mAdView: AdView = itemView.findViewById(R.id.adView)
    private var adRequest: AdRequest = AdRequest.Builder().build()

    var addAsuna: FrameLayout = itemView.findViewById(R.id.addAsuna)

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private lateinit var auth: FirebaseAuth
    private val thumbnails: StorageReference = storage.reference.child("thumbnails")
    private var isLiked: TextView = itemView.findViewById(R.id.isLiked)

    @SuppressLint("HardwareIds")

    fun bind(card: Card) {
        if (card.id != "ADV") {
            counterTwo.text = card.currentCardNum.toString()
            counterFirst.text = card.allCardCount.toString()
            titleCard.text = card.title
            socialAll.text = card.commentsCount.toString()
            publish.text = card.likesCount.toString()
            isLiked.text = "0"

            auth = Firebase.auth

            var id = auth.currentUser?.uid

            if (id.isNullOrEmpty()) {
                id = "null"
            }

            thumbnails.child("${card.thumbPath}.jpeg")
                .downloadUrl
                .addOnSuccessListener {
                    Glide.with(parent.context)
                        .load(it)
                        .into(yogaIconGrand)
                    Glide.with(parent.context)
                        .load(it)
                        .into(image)

                }.addOnFailureListener { exception ->
                    Log.d("log", "get failed with ", exception)
                }

            db.collection("likes").document(card.id)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        if (document.contains(id)) {
                            if (document.data?.get(id) as Boolean) {
                                isLiked.text = "1"
                            }
                        }
                    }
                    if (auth.currentUser != null) {
                        if (isLiked.text == "1") {
                            likeImg.setImageResource(R.drawable.ic_baseline_favorite_24)
                        } else {
                            likeImg.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                        }
                    } else {
                        likeImg.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("log", "get failed with ", exception)
                }
            if (auth.currentUser != null) {
                layoutDate1.setOnClickListener {
                    if (isLiked.text == "1") {
                        card.likesCount -= 1
                        db.collection("likes").document(card.id)
                            .update(id, false)
                        db.collection("asunaRU").document(card.id)
                            .update("likes", card.likesCount)
                        publish.text = (card.likesCount).toString()
                        likeImg.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                        isLiked.text = "0"
                    } else {
                        card.likesCount += 1
                        db.collection("likes").document(card.id)
                            .update(id, true)
                        db.collection("asunaRU").document(card.id)
                            .update("likes", card.likesCount)
                        publish.text = (card.likesCount).toString()
                        likeImg.setImageResource(R.drawable.ic_baseline_favorite_24)
                        isLiked.text = "1"
                    }
                }
            }

            titleCard.setOnClickListener {
                openAsuna(card.id)
            }

            image.setOnClickListener {
                openAsuna(card.id)
            }

            social.setOnClickListener {
                openAsuna(card.id)
            }

            buttonSettings.setOnClickListener {
                val popupMenu = PopupMenu(parent.context, it)
                popupMenu.inflate(R.menu.option_menu)

                popupMenu.setOnMenuItemClickListener { item ->
                    return@setOnMenuItemClickListener when (item.itemId) {
                        R.id.item1 -> {
                            true
                        }
                        R.id.item2 -> {
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        } else {
            asunaCard.visibility = View.GONE
            ads.visibility = View.VISIBLE

            mAdView.loadAd(adRequest)
        }
    }

    private fun openAsuna(id: String) {
        val intent = Intent(
            parent.context,
            AsunaActivity::class.java
        )
        intent.putExtra("asunaID", id)
        parent.context.startActivity(intent)
    }
}