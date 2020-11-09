package com.simon.yoga_statica.classes

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds

class AdvController(private val ctx: Context) {
    fun init() {
        MobileAds.initialize(ctx) {
            Log.d("adsstatus", it.toString())
        }
    }

    fun createInterstitialAds(unitid: Int) : InterstitialAd {
        val adRequest = AdRequest.Builder().build()
        val interstitialAd = InterstitialAd(ctx)
        interstitialAd.adUnitId = ctx.getString(unitid)
        interstitialAd.loadAd(adRequest)

        return interstitialAd
    }

    fun createUnifiedAds(count: Int, unitid: Int, listening: AdUnifiedListening) {
        val builder: AdLoader.Builder =  AdLoader.Builder(ctx, ctx.getString(unitid))
        builder.forUnifiedNativeAd(listening)
        builder.withAdListener(listening)

        val adLoad: AdLoader = builder.build()
        adLoad.loadAds(AdRequest.Builder().build(), count)

        listening.Adloader = adLoad
    }

    fun createUnifiedAd(unitid: Int, listening: AdUnifiedListening) {
        val builder: AdLoader.Builder =  AdLoader.Builder(ctx, ctx.getString(unitid))
        builder.forUnifiedNativeAd(listening)
        builder.withAdListener(listening)

        val adLoad: AdLoader = builder.build()
        adLoad.loadAd(AdRequest.Builder().build())

        listening.Adloader = adLoad
    }

    fun destroyUnifiedAd(unitid: Int) {
        AdLoader.Builder(ctx, ctx.getString(unitid))
            .forUnifiedNativeAd {
                it.destroy()
                return@forUnifiedNativeAd
            }.build()
    }
}