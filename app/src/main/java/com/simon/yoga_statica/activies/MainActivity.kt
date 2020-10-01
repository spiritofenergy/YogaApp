package com.simon.yoga_statica.activies

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.classes.User
import com.simon.yoga_statica.fragments.AsunaFragment
import com.simon.yoga_statica.fragments.AsunaListFragment
import com.simon.yoga_statica.fragments.FavoriteListFragment
import com.simon.yoga_statica.fragments.ProfileFragment
import java.util.*

class MainActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private lateinit var gso: GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var auth: FirebaseAuth

    private lateinit var container: FrameLayout

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)

        if (!prefs.contains(APP_PREFERENCES_THEME) || auth.currentUser == null) {
            setTheme(R.style.AppTheme)
        } else {
            when (prefs.getString(APP_PREFERENCES_THEME, "default")) {
                "coffee" -> setTheme(R.style.CoffeeAppTheme)
                "default" -> setTheme(R.style.AppTheme)
            }
        }
        setContentView(R.layout.activity_main)
        val count = supportFragmentManager.backStackEntryCount
        Log.d("c", count.toString())
        if (count == 0)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        else
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val profile = intent.getBooleanExtra("profile", false)
        intent.putExtra("profile", false)

        Log.d("lang", Locale.getDefault().displayLanguage)

        container = findViewById(R.id.fragmentContainer)

        if (count == 0)
            openMain()

        if (profile) {
            Log.d("prof", "true")
            openProfile()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_main, menu)

        if (auth.currentUser != null) {
            menu?.getItem(3)?.isVisible = false
            menu?.getItem(0)?.isVisible = true
            menu?.getItem(1)?.isVisible = true
            menu?.getItem(2)?.isVisible = true
            menu?.getItem(4)?.isVisible = true
        }

        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val count = supportFragmentManager.backStackEntryCount
        Log.d("c", count.toString())
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.favoriteBut -> {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                val listFragment = FavoriteListFragment()
                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                if (container.tag == "usual_display") {
                    with (transaction) {
                        replace(R.id.fragmentContainer, listFragment)
                        addToBackStack(null)
                        commit()
                    }
                } else {
                    with (transaction) {
                        replace(R.id.list_frag, listFragment)
                        addToBackStack(null)
                        commit()
                    }
                }

                true
            }
            R.id.openProfile -> {
                openProfile()

                true
            }
            R.id.google_signin -> {
                val signInIntent: Intent = mGoogleSignInClient.signInIntent
                startActivityForResult(signInIntent, 123)

                true
            }
            R.id.signout -> {
                Firebase.auth.signOut()
                mGoogleSignInClient.revokeAccess()
                val intent = intent
                finish()
                startActivity(intent)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        if (count == 1) supportActionBar?.setDisplayHomeAsUpEnabled(false)

        Log.d("c", count.toString())

        super.onBackPressed()
    }

    private fun openProfile() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (auth.currentUser != null) {
            val listFragment = ProfileFragment()
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            if (container.tag == "usual_display") {
                with (transaction) {
                    replace(R.id.fragmentContainer, listFragment)
                    addToBackStack(null)
                    commit()
                }
            } else {
                with (transaction) {
                    replace(R.id.list_frag, listFragment)
                    addToBackStack(null)
                    commit()
                }
            }
        }
    }

    private fun openMain() {
        val listFragment = AsunaListFragment()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()

        if (container.tag == "usual_display") {
            with (transaction) {
                replace(R.id.fragmentContainer, listFragment)
                commit()
            }
        } else {
            with (transaction) {
                replace(R.id.list_frag, listFragment)
                commit()
            }

            val asuna = AsunaFragment()
            asuna.setAsuna("asuna01")
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, asuna)
                .commit()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

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
                baseContext, "Authentication failed.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("login", "signInWithCredential:success")

                    val user = auth.currentUser
                    addUserToDatabase(user)

                    val intent = intent
                    finish()
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("login", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
    }

    private fun addUserToDatabase(currentUser: FirebaseUser?) {
        val user = hashMapOf(
            "id" to currentUser?.uid.toString().trim(),
            "email" to currentUser?.email.toString().trim(),
            "name" to "User",
            "root" to "user",
            "countAsuns" to 0,
            "status" to 1,
            "sec" to 30,
            "colorTheme" to "default",
            "photo" to ""
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

    fun setDisplayBack(show: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(show)
    }
}


