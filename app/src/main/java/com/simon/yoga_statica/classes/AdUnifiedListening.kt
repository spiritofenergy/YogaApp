package com.simon.yoga_statica.classes

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.formats.UnifiedNativeAd

open class AdUnifiedListening: AdListener(), UnifiedNativeAd.OnUnifiedNativeAdLoadedListener {
    lateinit var Adloader: AdLoader

    override fun onUnifiedNativeAdLoaded(ads: UnifiedNativeAd?) {
    }


}
