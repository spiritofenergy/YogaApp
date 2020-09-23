package com.simon.yoga_statica.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.classes.User

class ProfileFragment : Fragment() {
    private val db = Firebase.firestore

    private lateinit var auth: FirebaseAuth
    private var user = User()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fraagment_profile, container, false)

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

        return rootView
    }
}