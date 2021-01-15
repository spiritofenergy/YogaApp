package com.simon.yoga_statica.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class PromocodeFragmentViewModel : ViewModel() {

    private val db = Firebase.firestore

    private var auth: FirebaseAuth = Firebase.auth

    private val _promocodeIsExist: MutableLiveData<Boolean> = MutableLiveData()
    private val _promocode: MutableLiveData<String> = MutableLiveData()
    private val _sale: MutableLiveData<Int?> = MutableLiveData()
    private val _usesPromo: MutableLiveData<String?> = MutableLiveData()
    private val _setPromo: MutableLiveData<Boolean> = MutableLiveData()
    private val _countUsed: MutableLiveData<Int> = MutableLiveData()

    fun promocodeIsExist() : LiveData<Boolean> {
        db.collection("users")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        _promocodeIsExist.value = document.contains("promocode_id")
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("home", "Error getting documents: ", exception)
            }

        return _promocodeIsExist
    }

    fun getPromocode() : LiveData<String> {
        db.collection("users")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val id = document.get("promocode_id").toString()

                        db.collection("promocodes")
                            .document(id)
                            .get()
                            .addOnSuccessListener {
                                _promocode.value = it["promocode"] as String
                                _countUsed.value = it.get("used_times", Int::class.java) ?: 0
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("home", "Error getting documents: ", exception)
            }

        return _promocode
    }

    fun getSale() : LiveData<Int?> {
        db.collection("users")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        _sale.value = document.get("sale", Int::class.java)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("home", "Error getting documents: ", exception)
            }

        return _sale
    }

    fun getCount() : LiveData<Int> {

        return _countUsed
    }

    fun getUsesPromo() : LiveData<String?> {
        db.collection("users")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        _usesPromo.value = document.getString("uses_promocode_id")
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("home", "Error getting documents: ", exception)
            }

        return _usesPromo
    }

    fun setPromocode(promo: String) : LiveData<Boolean?> {
        _setPromo.value = null
        var promoId: String = ""

        db.collection("promocodes")
            .whereEqualTo("promocode", promo)
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty){
                    for (doc in it) {

                        promoId = doc.id
                        val times = doc.get("used_times", Int::class.java) ?: 0

                        db.collection("promocodes")
                            .document(promoId)
                            .update("used_times", times + 1)
                            .addOnSuccessListener {
                                Log.d("used", times.toString())
                            }
                            .addOnFailureListener {
                                Log.d("used", it.localizedMessage)
                            }
                    }

                    db.collection("users")
                        .whereEqualTo("id", auth.currentUser?.uid)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                for (document in documents) {

                                    db.collection("users")
                                        .document(document.id)
                                        .update("uses_promocode_id", promo,
                                            "sale", 10)
                                        .addOnSuccessListener {

                                            db.collection("users")
                                                .whereEqualTo("promocode_id", promoId)
                                                .get()
                                                .addOnSuccessListener { users ->
                                                    if (!users.isEmpty) {
                                                        for (user in users) {
                                                            db.collection("users")
                                                                .document(user.id)
                                                                .update("sale", 50)
                                                        }
                                                    }
                                                }
                                            _sale.value = 10
                                            _setPromo.value = true
                                        }

                                }
                            }
                        }
                } else {
                    _setPromo.value = false
                }
            }

        return _setPromo
    }

}