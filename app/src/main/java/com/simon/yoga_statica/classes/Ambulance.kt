package com.simon.yoga_statica.classes

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.json.JSONObject
import kotlin.coroutines.resume
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
                }

        }
    }

    suspend fun setData(data: String, id: String?) : Boolean {
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
                    .addOnSuccessListener { continuation.resume(true) }
                    .addOnCanceledListener { continuation.resume(false) }
            }

        }
    }

}