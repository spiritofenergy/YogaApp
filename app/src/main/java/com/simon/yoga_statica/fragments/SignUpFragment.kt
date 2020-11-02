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
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.activies.MainActivity

class SignUpFragment : Fragment() {

    private val db = Firebase.firestore

    private val auth = Firebase.auth

    private lateinit var name: String

    private lateinit var nameTextView: EditText
    private lateinit var emailTextView: EditText
    private lateinit var passwordTextView: EditText

    private lateinit var signUp: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_signup, container, false)

        nameTextView = rootView.findViewById(R.id.nameUserSignUp)
        emailTextView = rootView.findViewById(R.id.emailUserSignUp)
        passwordTextView = rootView.findViewById(R.id.passwordUserAuth3)

        signUp = rootView.findViewById(R.id.signUpEmail)
        signUp.setOnClickListener {
            name = nameTextView.text.toString()
            val email = emailTextView.text
            val password = passwordTextView.text
            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                firebaseNewAuthWithEmail(email.toString(), password.toString())
            } else {
                Toast.makeText(activity, "Заполните все поля",
                    Toast.LENGTH_SHORT).show()
            }
        }

        return rootView
    }

    private fun firebaseNewAuthWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    Log.d("auth", "createUserWithEmail:success")
                    val user = auth.currentUser

                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }

                    user!!.updateProfile(profileUpdates)

                    addUserToDatabase(user)

                    openMain()
                } else {
                    Log.w("auth", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(activity, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
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