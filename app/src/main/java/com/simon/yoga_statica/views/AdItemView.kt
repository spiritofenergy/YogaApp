package com.simon.yoga_statica.views

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.simon.yoga_statica.R
import com.simon.yoga_statica.classes.Ad
import com.simon.yoga_statica.classes.AdUnifiedListening
import com.simon.yoga_statica.classes.AdvController

class AdItemView(inflater: LayoutInflater, private val parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(
    R.layout.ad_item, parent, false)) {

    private var templateView: TemplateView = itemView.findViewById(R.id.ad_native)

    private lateinit var advController: AdvController

    fun bind(ad: Ad) {
        val style: NativeTemplateStyle = NativeTemplateStyle
            .Builder()
            .withMainBackgroundColor(
                ColorDrawable(Color.parseColor("#ffffff"))
            )
            .build()
        templateView.setStyles(style)
        Log.d("FROMAD", ad.id.toString())
        if (ad.id != null)
            templateView.setNativeAd(ad.id)
    }
}