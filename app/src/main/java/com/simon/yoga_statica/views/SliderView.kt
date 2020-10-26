package com.simon.yoga_statica.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.simon.yoga_statica.R

class SliderView(inflater: LayoutInflater, private val parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(
    R.layout.slider_image_item, parent, false)) {

    var image: ImageView = itemView.findViewById(R.id.slider_image)
    private var progressBarRecyclerView: ProgressBar = itemView.findViewById(R.id.progressBarRecyclerView)
    private val storage = Firebase.storage
    private val thumbnails: StorageReference = storage.reference.child("thumbnails")

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("HardwareIds", "UseCompatLoadingForDrawables")
    fun bind(uri: String) {

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

        progressBarRecyclerView.indeterminateDrawable = when (theme) {
            "default" -> parent.context.getDrawable(R.drawable.spinner_ring)
            "red" -> parent.context.getDrawable(R.drawable.spinner_ring_red)
            "orange" -> parent.context.getDrawable(R.drawable.spinner_ring_orange)
            "lime" -> parent.context.getDrawable(R.drawable.spinner_ring_lime)
            "coffee" -> parent.context.getDrawable(R.drawable.spinner_ring_coffee)
            else ->  parent.context.getDrawable(R.drawable.spinner_ring)
        }

        thumbnails.child("${uri}.jpeg")
            .downloadUrl
            .addOnSuccessListener {
                Glide.with(parent.context)
                    .load(it)
                    .addListener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBarRecyclerView.visibility = View.GONE
                            return false
                        }

                    })
                    .into(image)

            }.addOnFailureListener { exception ->
                Log.d("log", "get failed with ", exception)
            }
    }
}