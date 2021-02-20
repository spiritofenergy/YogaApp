package com.simon.yoga_statica.classes

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Ambulance {

    private val db = Firebase.firestore

    suspend fun getId(id: String) : String? {
        return suspendCoroutine { continuation ->

            db.collection("users")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            val idAmbulance = document.get("ambulance_id", String::class.java)
                            continuation.resume(idAmbulance)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("home", "Error getting documents: ", exception)
                    continuation.resumeWithException(exception)
                }

        }
    }

    private fun addId(user: String, id: String) {
        db.collection("users")
            .whereEqualTo("id", user)
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (doc in it) {
                        db.collection("users")
                            .document(doc.id)
                            .update("ambulance_id", id)
                    }
                }
            }
    }

    suspend fun getData(id: String) : JSONObject {
        return suspendCoroutine { continuation ->

            db.collection("ambulance")
                .document(id)
                .get()
                .addOnSuccessListener { document ->

                    var res = document.get("data", String::class.java)
                    res = res ?: ""
                    continuation.resume(JSON.Response(res))

                }
                .addOnFailureListener { exception ->
                    Log.w("home", "Error getting documents: ", exception)
                    continuation.resumeWithException(exception)
                }

        }
    }

    suspend fun setData(data: String, id: String?, user: String? = null) : Boolean {
        return suspendCoroutine { continuation ->

            if (id != null) {
                db.collection("ambulance")
                    .document(id)
                    .update("data", data)
                    .addOnSuccessListener { continuation.resume(true) }
                    .addOnCanceledListener { continuation.resume(false) }
            } else {
                db.collection("ambulance")
                    .add(
                        hashMapOf(
                            "data" to data
                        )
                    )
                    .addOnSuccessListener {
                        if (user != null) {
                            addId(user, it.id)
                        }
                        continuation.resume(true)
                    }
                    .addOnCanceledListener { continuation.resume(false) }
            }

        }
    }

}