package com.simon.yoga_statica.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.activies.MainActivity
import kotlin.math.sign

class AuthFragment : Fragment() {
    private val db = Firebase.firestore
    private lateinit var gso: GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val auth = Firebase.auth

    private lateinit var emailText: EditText
    private lateinit var passwordText: EditText

    private lateinit var signInGoogle: Button
    private lateinit var signInEmail: Button
    private lateinit var signUpOpen: Button
    private lateinit var openWithoutAuth: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_auth, container, false)

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(activity!!, gso)

        signInGoogle = rootView.findViewById(R.id.google_signIn_auth)
        signInGoogle.setOnClickListener {
            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, 123)
        }

        emailText = rootView.findViewById(R.id.emailUserAuth)
        passwordText = rootView.findViewById(R.id.passwordUserAuth)

        signInEmail = rootView.findViewById(R.id.signInEmail)
        signInEmail.setOnClickListener {
            val email = emailText.text
            val password = passwordText.text

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuthWithEmail(email.toString(), password.toString())
            } else {
                Toast.makeText(activity, "Заполните все поля",
                    Toast.LENGTH_SHORT).show()
            }
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
            openMain()
        }

        return rootView
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
                activity, "Authentication failed.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity!!) { task ->
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
                        activity, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
    }

    private fun firebaseAuthWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    Log.d("auth", "signInWithEmail:success")
                    val user = auth.currentUser

                    openMain()
                } else {
                    Log.w("auth", "signInWithEmail:failure", task.exception)
                    Toast.makeText(activity, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
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

    private fun openMain() {
        val intent = Intent(
            activity,
            MainActivity::class.java
        )

        startActivity(intent)
    }
}