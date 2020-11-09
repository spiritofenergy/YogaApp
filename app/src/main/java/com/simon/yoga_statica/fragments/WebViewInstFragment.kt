package com.simon.yoga_statica.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.activies.MainActivity
import com.simon.yoga_statica.classes.MyWebViewClient
import kotlinx.android.synthetic.main.fragment_auth_inst.view.*

class WebViewInstFragment : Fragment() {
    private var redirectUrl = "https://github.com/spiritofenergy"
    private lateinit var initialUrl: String

    private lateinit var webInst: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_auth_inst, container, false)

        initialUrl = "https://api.instagram.com/oauth/authorize?client_id=${getString(R.string.instagram_app_id)}&redirect_uri=$redirectUrl&scope=user_profile,user_media&response_type=code"
        webInst = rootView.webInst

        webInst.settings.javaScriptEnabled = true

        // All functions And Auth Algorithm In MyWebViewClient Class
        val web = MyWebViewClient(
            activity,
            rootView.reloadText
        )

        webInst.webViewClient = web
        webInst.loadUrl(initialUrl)

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webInst.destroy()
    }
}