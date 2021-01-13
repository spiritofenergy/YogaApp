package com.simon.yoga_statica.classes

import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.simon.yoga_statica.R

class Promocode {

    private val db = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth

    /**
     * Метод получения рандомной 28-ми символьной цифробуквенной строки
     *
     * @return Рандомная цифробуквенная строка
     */
    fun createAndSavePromocode() : String {
        val allowedChars = ('A'..'Z') + ('a'..'z')
        val promocode = (1..6)
            .map { allowedChars.random() }
            .joinToString("")

        db.collection("promocodes")
            .add(
                hashMapOf(
                    "promocode" to promocode
                )
            )
            .addOnSuccessListener { promocodeRow ->

                db.collection("users")
                    .whereEqualTo("id", auth.currentUser?.uid)
                    .get()
                    .addOnSuccessListener {
                        if (!it.isEmpty) {
                            for (doc in it) {
                                db.collection("users")
                                    .document(doc.id)
                                    .update("promocode_id", promocodeRow.id)
                            }
                        }
                    }
            }
        Log.d("isExistPromocode", promocode)
        return promocode
    }
}