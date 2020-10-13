package com.simon.yoga_statica.views

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.simon.yoga_statica.R

class SliderView(inflater: LayoutInflater, private val parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(
    R.layout.slider_image_item, parent, false)) {

    var image: ImageView = itemView.findViewById(R.id.slider_image)
    private val storage = Firebase.storage
    private val thumbnails: StorageReference = storage.reference.child("thumbnails")

    fun bind(uri: String) {
        thumbnails.child("${uri}.jpeg")
            .downloadUrl
            .addOnSuccessListener {
                Glide.with(parent.context)
                    .load(it)
                    .into(image)

            }.addOnFailureListener { exception ->
                Log.d("log", "get failed with ", exception)
            }
    }
}