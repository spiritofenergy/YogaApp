package com.simon.yoga_statica.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.activies.MainActivity

class AuthEmailFragment : Fragment() {

    private val auth = Firebase.auth

    private lateinit var emailText: EditText
    private lateinit var passwordText: EditText

    private lateinit var signInEmail: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_email_auth, container, false)
        Log.d("email", "HELLO")
        emailText = rootView.findViewById(R.id.emailUserAuth)
        passwordText = rootView.findViewById(R.id.passwordUserAuth)

        signInEmail = rootView.findViewById(R.id.signInEmail)
        signInEmail.setOnClickListener {
            val email = emailText.text
            val password = passwordText.text

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuthWithEmail(email.toString(), password.toString())
            } else {
                Toast.makeText(activity, getString(R.string.put_all_fields),
                    Toast.LENGTH_SHORT).show()
            }
        }

        return rootView
    }

    private fun firebaseAuthWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d("auth", "signInWithEmail:success")
                    val user = auth.currentUser

                    openMain()
                } else {
                    Log.w("auth", "signInWithEmail:failure", task.exception)
                    Toast.makeText(activity, getString(R.string.auth_fail),
                        Toast.LENGTH_SHORT).show()
                }
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