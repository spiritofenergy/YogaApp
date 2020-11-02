package com.simon.yoga_statica.adapters

import android.content.Context
import android.media.Image
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class OpenAsanaAdapter(private val listID: List<String>, private val ctx: Context) : BaseAdapter() {
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val thumbnails: StorageReference = storage.reference.child("thumbnails")

    override fun getCount(): Int = listID.size

    override fun getItem(position: Int): Any = listID[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView: ImageView

        if (convertView == null) {
            imageView = ImageView(ctx)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.layoutParams = ViewGroup.LayoutParams(140, 140)
        } else {
            imageView = convertView as ImageView
        }

        db.collection("openAsunaRU")
            .document(listID[position])
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
                                Glide.with(ctx)
                                    .load(it)
                                    .into(imageView)
                        }.addOnFailureListener { exception ->
                            Log.d("log", "get failed with ", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("gets", "Error getting documents.", exception)
            }


        return imageView
    }

}