package com.simon.yoga_statica.activies

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.adapters.CardAdapter
import com.simon.yoga_statica.classes.Card
import com.simon.yoga_statica.classes.User
import com.simon.yoga_statica.fragments.AsunaListFragment
import com.simon.yoga_statica.fragments.FavoriteListFragment
import com.simon.yoga_statica.interfaces.OnRecyclerItemClickListener
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var cardsRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private val db = Firebase.firestore
    private lateinit var gso: GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var auth: FirebaseAuth
    private var user = User()

    private var cardsArr = mutableListOf<Card>()
    private var addsAsuna = mutableListOf<String>()

    private lateinit var container: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("lang", Locale.getDefault().displayLanguage)

        container = findViewById(R.id.fragmentContainer)


        if (container.tag == "usual_display") {
            val listFragment = AsunaListFragment()
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, listFragment)
            transaction.commit()
        }

        auth = Firebase.auth

        db.collection("users")
            .whereEqualTo("uid", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        user.name = document["name"].toString()
                        user.id = document["id"].toString()
                        user.email = document["email"].toString()
                        user.status = document["status"].toString()
                        user.sec = (document["sec"] as Long).toInt()
                        user.colorTheme = document["colorTheme"].toString()
                        user.countAsuns = (document["countAsuns"] as Long).toInt()
                        user.photo = document["photo"].toString()
                    }
                }

            }
            .addOnFailureListener { exception ->
                Log.w("home", "Error getting documents: ", exception)
            }
//
//        fab.setOnClickListener {
//            val intent = Intent(
//                this,
//                ActionActivity::class.java
//            )
//
//            intent.putExtra("listAsuna", ArrayList(addsAsuna))
//            addsAsuna.clear()
//            startActivity(intent)
//            fab.visibility = View.GONE
//        }

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

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
        return when (item.itemId) {
            R.id.favoriteBut -> {
                if (container.tag == "usual_display") {
                    val listFragment = FavoriteListFragment()
                    val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragmentContainer, listFragment)
                    transaction.commit()
                }

                true
            }
            R.id.openProfile -> {
                val intent = Intent(
                    this,
                    ProfileActivity::class.java
                )
                startActivity(intent)
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
            "status" to "newer",
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
}


