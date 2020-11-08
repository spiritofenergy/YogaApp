package com.simon.yoga_statica.classes

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.activies.MainActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MyWebViewClient(private val activity: Activity?, private val text: TextView, private val error: String, private val id: String, private val secure: String) : WebViewClient() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val urlToken = "https://api.instagram.com/oauth/access_token"
    private val redirectUrl = "https://github.com/spiritofenergy"

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if (request.url.toString().contains("l.instagram")) {
            view.visibility = View.GONE
            text.visibility = View.VISIBLE
        }

        if (request.url.toString().contains("code=(.+)#".toRegex())) {
            val match = Regex("code=(.+)#").find(request.url.toString())!!
            val code = match.destructured.component1()
            Thread {
                val json = getTokenAndUserID(code)
                val obj = JSON.Response(json)
                val token = obj["access_token"] as String
                val idUser = obj["user_id"] as Long

               val userJSON = getUser(
                    idUser,
                    token
                )
                val user = JSON.Response(userJSON)

                val jsonToken = getFirebaseToken(user["id"] as String)
                val tokenFirebase = JSON.Response(jsonToken)["customToken"] as String

                if (tokenFirebase != "")
                    firebaseAuthWithGoogle(tokenFirebase, user["username"] as String)

            }.start()
        }

        view.loadUrl(request.url.toString())
        return true
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

        if (url.contains("l.instagram")) {
            view.visibility = View.GONE
            text.visibility = View.VISIBLE
        }

        if (url.contains("code=(.+)#".toRegex())) {
            val match = Regex("code=(.+)#").find(url)!!
            val code = match.destructured.component1()
            Log.d("code", code)
        }

        view.loadUrl(url)
        return true
    }

    private fun getTokenAndUserID(code: String) : String {
        val response = StringBuffer()
        var reqParam = URLEncoder.encode("client_id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8")
        reqParam += "&" + URLEncoder.encode("client_secret", "UTF-8") + "=" + URLEncoder.encode(secure, "UTF-8")
        reqParam += "&" + URLEncoder.encode("grant_type", "UTF-8") + "=" + URLEncoder.encode("authorization_code", "UTF-8")
        reqParam += "&" + URLEncoder.encode("redirect_uri", "UTF-8") + "=" + URLEncoder.encode(redirectUrl, "UTF-8")
        reqParam += "&" + URLEncoder.encode("code", "UTF-8") + "=" + URLEncoder.encode(code, "UTF-8")
        val mURL = URL(urlToken)

        with(mURL.openConnection() as HttpURLConnection) {
            requestMethod = "POST"

            val wr = OutputStreamWriter(outputStream);
            wr.write(reqParam);
            wr.flush();

            Log.d("res","URL : $url")
            Log.d("res","Response Code : $responseCode")

            BufferedReader(InputStreamReader(inputStream)).use {
                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                Log.d("res", "Response : $response")
            }
        }

        return response.toString()
    }

    private fun getUser(userId: Long, token: String) : String {
        val response = StringBuffer()
        val mURL = URL("https://graph.instagram.com/$userId?fields=id,username&access_token=$token")

        with(mURL.openConnection() as HttpURLConnection) {
            requestMethod = "GET"

            println("URL : $url")
            println("Response Code : $responseCode")

            BufferedReader(InputStreamReader(inputStream)).use {
                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                it.close()
                println("Response : $response")
            }
        }

        return response.toString()
    }

    private fun getFirebaseToken(instagramToken: String) : String {
        val response = StringBuffer()
        val mURL = URL("https://us-central1-yogaapp-2a7ad.cloudfunctions.net/makeCustomToken?instagramToken=$instagramToken")

        with(mURL.openConnection() as HttpURLConnection) {
            requestMethod = "GET"

            println("URL : $url")
            println("Response Code : $responseCode")

            BufferedReader(InputStreamReader(inputStream)).use {
                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                it.close()
                println("Response : $response")
            }
        }

        return response.toString()
    }

    private fun firebaseAuthWithGoogle(idToken: String, name: String) {
        auth.signInWithCustomToken(idToken)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("login", "signInWithInstagram:success")

                    val user = auth.currentUser

                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }

                    user!!.updateProfile(profileUpdates)

                    addUserToDatabase(user)

                    openMain()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("login", "signInWithInstagram:failure", task.exception)
                    Toast.makeText(
                        activity, error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun addUserToDatabase(currentUser: FirebaseUser?) {
        val user = hashMapOf(
            "id" to currentUser?.uid.toString().trim(),
            "root" to "user",
            "countAsuns" to 0,
            "status" to 1
        )

        db.collection("users")
            .whereEqualTo("id", currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty)
                    db.collection("users")
                        .add(user)
            }
            .addOnFailureListener { exception ->
                Log.w("home", "Error getting documents: ", exception)
            }
    }

    private fun openMain() {
        val intent = Intent(
            activity,
            MainActivity::class.java
        )
        activity?.startActivity(intent)
        activity?.finish()
    }
}