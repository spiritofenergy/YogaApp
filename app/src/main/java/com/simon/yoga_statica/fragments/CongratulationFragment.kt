package com.simon.yoga_statica.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.activies.ActionActivity
import kotlin.properties.Delegates

class CongratulationFragment : Fragment() {

    private val db = Firebase.firestore

    private val auth = Firebase.auth

    private var countAsuns: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_congratulations, container, false)

        (activity as ActionActivity).setDisplayBack(true)
        (activity as ActionActivity).finish = true

        db.collection("users")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {

                        val countAsunsUser = (document["countAsuns"] as Long).toInt()

                        if (countAsunsUser + countAsuns >= 100) {
                            db.collection("users")
                                .document(document.id)
                                .update(
                                    mapOf(
                                        "countAsuns" to countAsunsUser + countAsuns,
                                        "status" to 2
                                    )
                                )
                        } else if (countAsunsUser + countAsuns >= 500) {
                            db.collection("users")
                                .document(document.id)
                                .update(
                                    mapOf(
                                        "countAsuns" to countAsunsUser + countAsuns,
                                        "status" to 3

                                    )
                                )
                        } else {
                            db.collection("users")
                                .document(document.id)
                                .update("countAsuns", countAsunsUser + countAsuns)
                        }

                    }


                }

            }
            .addOnFailureListener { exception ->
                Log.w("home", "Error getting documents: ", exception)
            }

        return rootView
    }

    fun setCount(count: Int) {
        countAsuns = count
    }
}