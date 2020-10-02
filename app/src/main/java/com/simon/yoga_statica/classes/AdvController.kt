package com.simon.yoga_statica.classes

import android.content.Context
import com.google.android.gms.ads.MobileAds

class AdvController(private val ctx: Context) {
    fun init() {
        MobileAds.initialize(ctx) {

        }
    }

    fun createUnifiedAds(count: Int, unitid: Int, listening: AdUnifiedListening) {

    }
}