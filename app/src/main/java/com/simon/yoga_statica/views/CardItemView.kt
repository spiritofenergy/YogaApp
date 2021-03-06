package com.simon.yoga_statica.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.simon.yoga_statica.R
import com.simon.yoga_statica.classes.Card
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.simon.yoga_statica.adapters.SliderAdapter
import com.simon.yoga_statica.interfaces.OnRecyclerItemClickListener

class CardItemView(inflater: LayoutInflater, private val parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_view, parent, false)) {
    private var counterTwo: TextView = itemView.findViewById(R.id.counterTwo)
    private var counterFirst: TextView = itemView.findViewById(R.id.counterFirst)
    var titleCard: TextView = itemView.findViewById(R.id.asanaTitle)
    private var nameTwo: TextView = itemView.findViewById(R.id.ddLongDescription)
    private var socialAll: TextView = itemView.findViewById(R.id.socialAll)
    private var publish: TextView = itemView.findViewById(R.id.publish)
    private var layoutDate1: FrameLayout = itemView.findViewById(R.id.layoutDate1)
    var social: FrameLayout = itemView.findViewById(R.id.social)
    private var likeImg: ImageView = itemView.findViewById(R.id.likeImg)
    private var yogaIconGrand: ImageView = itemView.findViewById(R.id.yogaIconGrand)
    var image: ViewPager2 = itemView.findViewById(R.id.addedImage)
    var imgFrame: FrameLayout = itemView.findViewById(R.id.imgFrame)
    private var buttonSettings: ImageView = itemView.findViewById(R.id.buttonSettings)
    private var commentImg: ImageView = itemView.findViewById(R.id.commentImg)
    private var lane: TextView = itemView.findViewById(R.id.lane)
    private var openAsans: LinearLayout = itemView.findViewById(R.id.openAsans)

    var addAsuna: FrameLayout = itemView.findViewById(R.id.addAsuna)

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private lateinit var auth: FirebaseAuth
    private val thumbnails: StorageReference = storage.reference.child("thumbnails")
    private var isLiked: TextView = itemView.findViewById(R.id.isLiked)
    private lateinit var fragmentManager: FragmentManager

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"

    @SuppressLint("InflateParams")
    fun bind(card: Card, fragmentManager: FragmentManager, listener: OnRecyclerItemClickListener?) {

        this.fragmentManager = fragmentManager

        auth = Firebase.auth

        prefs = parent.context.getSharedPreferences("settings", Context.MODE_PRIVATE)!!

        val theme = if (!prefs.contains(APP_PREFERENCES_THEME) || auth.currentUser == null) {
            "default"
        } else {
            when (prefs.getString(APP_PREFERENCES_THEME, "default")) {

                "default" -> "default"
                "red"    -> "red"
                "orange" -> "orange"
                "lime"  -> "lime"
                "coffee" -> "coffee"
                else     -> "default"
            }
        }

        counterTwo.text = card.currentCardNum.toString()
        counterFirst.text = card.allCardCount.toString()
        titleCard.text = card.title
        socialAll.text = card.commentsCount.toString()
        publish.text = card.likesCount.toString()
        nameTwo.text = card.shortDesc
        isLiked.text = "0"

        var opensList: List<String> = mutableListOf()
        if (card.openAsans != "") {
            opensList = card.openAsans.split(" ")
        }

        openAsans.removeAllViews()
        for (openID in opensList) {
            if (openID != "null") {

                Log.d("asdOPEN", card.id + " " + openID)

                val inflater =
                    parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val rowView: View = inflater.inflate(R.layout.open_photo, null)
                openAsans.addView(rowView, openAsans.childCount)

                Log.d("asd", openID)
                db.collection("openAsunaRU")
                    .document(openID)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            thumbnails.child(
                                "${
                                    document.data?.get("thumbPath").toString().split(" ")[0]
                                }.jpeg"
                            )
                                .downloadUrl
                                .addOnSuccessListener {
                                    Glide.with(parent.context)
                                        .load(it)
                                        .into(rowView.findViewById(R.id.openAsanaImage))
                                }.addOnFailureListener { exception ->
                                    Log.d("log", "get failed with $openID ", exception)
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("gets", "Error getting documents.", exception)
                    }
            }
        }



        var id = auth.currentUser?.uid

        if (id.isNullOrEmpty()) {
            id = "null"
        }

        commentImg.setImageResource(
            parent.resources.getIdentifier(
                "ic_chat_bubble_outline_black_24dp_$theme",
                "drawable",
                parent.context.packageName
            )
        )

        lane.setTextColor(
            when (theme) {
                "default" -> R.color.colorPrimary
                "red" -> R.color.colorPrimaryDark_red
                "orange" -> R.color.colorPrimaryDark_orange
                "lime" -> R.color.colorPrimaryDark_lime
                "coffee" -> R.color.colorPrimaryCoffee
                else ->  R.color.colorPrimary
            }
        )

        buttonSettings.setImageResource(
            parent.resources.getIdentifier(
                "ic_more_horiz_black_24dp_$theme",
                "drawable",
                parent.context.packageName
            )
        )
        val images = card.thumbPath.split(" ")

        val sliderAdapter = SliderAdapter(images)
        if (listener != null)
            sliderAdapter.setOnClickOpenListener(listener, card.id)

        image.adapter = sliderAdapter
        Log.d("img", images.toString())

        thumbnails.child("${images[0]}.jpeg")
            .downloadUrl
            .addOnSuccessListener {
                Glide.with(parent.context)
                    .load(it)
                    .into(yogaIconGrand)
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
                        likeImg.setImageResource(
                            setLikeImage(
                                false,
                                theme
                            )
                        )

                    } else {
                        likeImg.setImageResource(
                            setLikeImage(
                                true,
                                theme
                            )
                        )
                    }
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
                    likeImg.setImageResource(
                        setLikeImage(
                            true,
                            theme
                        )
                    )
                    isLiked.text = "0"
                } else {
                    card.likesCount += 1
                    db.collection("likes")
                        .document(card.id)
                        .get()
                        .addOnSuccessListener { result ->
                            Log.d("res", result.toString())
                            if (!result.exists()) {
                                Log.d("test", "asd")
                                db.collection("likes").document(card.id)
                                    .set(
                                        hashMapOf(
                                            id to true
                                        )
                                    )
                            } else {
                                Log.d("test", "asv")
                                db.collection("likes").document(card.id)
                                    .update(id, true)
                            }
                        }
                    db.collection("asunaRU").document(card.id)
                        .update("likes", card.likesCount)
                    publish.text = (card.likesCount).toString()
                    likeImg.setImageResource(
                        setLikeImage(
                            false,
                            theme
                        )
                    )
                    isLiked.text = "1"
                }
            }
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
    }

    private fun setLikeImage(empty: Boolean, theme: String) : Int {
        return if (empty) {
            parent.resources.getIdentifier(
                "ic_favorite_border_black_24dp_$theme",
                "drawable",
                parent.context.packageName
            )
        } else {
            parent.resources.getIdentifier(
                "ic_baseline_favorite_24_$theme",
                "drawable",
                parent.context.packageName
            )
        }
    }
}

