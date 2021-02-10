package com.simon.yoga_statica.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.activies.MainActivity
import com.simon.yoga_statica.adapters.AuthAdapter

class AuthFragment : Fragment() {
    private val db = Firebase.firestore
    private lateinit var gso: GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager



    private val auth = Firebase.auth

    private lateinit var signInGoogle: Button
    private lateinit var instagramAuth: Button
    private lateinit var signUpOpen: Button
    private lateinit var openWithoutAuth: Button

    private lateinit var tabLayout: TabLayout
    private lateinit var tabsItems: ViewPager2

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_AUTH = "authWithin"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_auth, container, false)

        prefs = activity?.getSharedPreferences("settings", Context.MODE_PRIVATE)!!

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        callbackManager = CallbackManager.Factory.create()

        tabLayout = rootView.findViewById(R.id.tabs)
        tabsItems = rootView.findViewById(R.id.tabs_items)
        tabsItems.adapter = AuthAdapter(this, tabLayout.tabCount)

        TabLayoutMediator(tabLayout, tabsItems) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "E-Mail"
                }
                1 -> {
                    tab.text = "Телефон"
                }
            }
        }.attach()

        signInGoogle = rootView.findViewById(R.id.google_signIn_auth)
        instagramAuth = rootView.findViewById(R.id.instagramAuth)
        signInGoogle.setOnClickListener {
            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, 123)
        }

        signUpOpen = rootView.findViewById(R.id.signUpOpen)
        signUpOpen.setOnClickListener {
            val listFragment = SignUpFragment()
            val transaction: FragmentTransaction? = fragmentManager?.beginTransaction()
            if (transaction != null)
                with (transaction) {
                    replace(R.id.splashFragment, listFragment)
                    commit()
                }
        }

        openWithoutAuth = rootView.findViewById(R.id.openWithoutAuth)
        openWithoutAuth.setOnClickListener {
            prefs
                .edit()
                .putBoolean(APP_PREFERENCES_AUTH, true)
                .apply()
            openMain()
        }
        instagramAuth.setOnClickListener {
            val listFragment = WebViewInstFragment()
            val transaction: FragmentTransaction? = fragmentManager?.beginTransaction()
            if (transaction != null)
                with (transaction) {
                    replace(R.id.splashFragment, listFragment)
                    addToBackStack(null)
                    commit()
                }
        }

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            Log.d("login", "signInWithEmail:success")
            firebaseAuthWithGoogle(account?.idToken.toString())
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(
                "err",
                "signInResult:failed code=" + e.statusCode
            )
            Toast.makeText(
                activity, getString(R.string.auth_fail),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("login", "signInWithCredential:success")

                    val user = auth.currentUser
                    addUserToDatabase(user)

                    openMain()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("login", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        activity, getString(R.string.auth_fail),
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
        startActivity(intent)
        activity?.finish()
    }
}