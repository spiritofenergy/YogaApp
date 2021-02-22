package com.simon.yoga_statica.classes

import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.viewmodels.PromocodeFragmentViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class Payment(fragment: Fragment, private val token: String, private val type: String, private val price: Double) {

    private val auth = Firebase.auth
    private var viewModel: PromocodeFragmentViewModel = ViewModelProvider(fragment).get(PromocodeFragmentViewModel::class.java)

    fun sendRequest() : String {
        Log.d("payDataTest", "CREATE")
        var reqParam = URLEncoder.encode("promo", "UTF-8") + "=" + URLEncoder.encode(token, "UTF-8")
        reqParam += "&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8")
        reqParam += "&" + URLEncoder.encode("price", "UTF-8") + "=" + URLEncoder.encode(price.toString(), "UTF-8")
        Log.d("payDataTest", "CREATE")
        if (auth.currentUser != null) {
            reqParam += "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(auth.currentUser!!.email ?: "", "UTF-8")
            reqParam += "&" + URLEncoder.encode("phone", "UTF-8") + "=" + URLEncoder.encode(auth.currentUser!!.phoneNumber ?: "", "UTF-8")
        } else {
            reqParam += "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode("nigmatullov@mail.ru", "UTF-8")
        }
        Log.d("payDataTest", "CREAT1E1")

        val mURL = URL("https://api.seostor.ru/sendPromo.php?$reqParam")

        viewModel.sendRequest(mURL)

        return ""
    }


}