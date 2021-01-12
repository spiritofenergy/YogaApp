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

class ProfileFragmentViewModel : ViewModel() {

    private val db = Firebase.firestore

    private var auth: FirebaseAuth = Firebase.auth

    private val _promocodeIsExist: MutableLiveData<Boolean> = MutableLiveData()
    private val _promocode: MutableLiveData<String> = MutableLiveData()

    fun promocodeIsExist() : LiveData<Boolean> {
        db.collection("users")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        _promocodeIsExist.value = document.contains("promocode")
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
                        _promocode.value = document.get("promocode").toString()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("home", "Error getting documents: ", exception)
            }

        return _promocode
    }

}