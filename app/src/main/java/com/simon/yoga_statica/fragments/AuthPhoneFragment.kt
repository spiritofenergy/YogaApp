package com.simon.yoga_statica.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.activies.MainActivity
import kotlinx.android.synthetic.main.fragment_phone_auth.view.*
import java.util.concurrent.TimeUnit

class AuthPhoneFragment : Fragment() {
    private val db = Firebase.firestore

    private val auth = Firebase.auth

    private lateinit var phoneText: EditText
    private lateinit var codeText: EditText

    private lateinit var checkPhone: Button
    private lateinit var signInPhone: Button
    private var verificationInProgress = false

    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_phone_auth, container, false)


        if (savedInstanceState != null) {
            verificationInProgress = savedInstanceState.getBoolean("verification")
        }

        auth.useAppLanguage()

        phoneText = rootView.phoneUserAuth
        codeText = rootView.codePhoneAuth

        checkPhone = rootView.checkPhone
        checkPhone.setOnClickListener {
            val phone = phoneText.text
            if (phone.isNotEmpty()) {
                checkPhone(phone.toString())
            } else {
                Toast.makeText(activity, "Заполните все поля",
                    Toast.LENGTH_SHORT).show()
            }
        }

        signInPhone = rootView.signInPhone
        signInPhone.setOnClickListener {
            val code = codeText.text
            if (verificationInProgress)
                if (code.isNotEmpty()) {
                    verifyPhoneNumberWithCode(storedVerificationId, code.toString())
                } else {
                    Toast.makeText(activity, "Заполните все поля",
                        Toast.LENGTH_SHORT).show()
                }
        }

        return rootView
    }

    private fun checkPhone(phone: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phone,
            60,
            TimeUnit.SECONDS,
            activity!!,
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    verificationInProgress = false

                    firebaseAuthWithPhone(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.d("e", e.toString())
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken) {

                    storedVerificationId = verificationId
                    resendToken = token

                    checkPhone.visibility = View.GONE
                    signInPhone.visibility = View.VISIBLE
                }
            })
        verificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)

        firebaseAuthWithPhone(credential)
    }

    private fun firebaseAuthWithPhone(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    Log.d("auth", "signInWithCredential:success")

                    val user = task.result?.user
                    addUserToDatabase(user)

                    openMain()
                } else {
                    Log.w("auth", "signInWithCredential:failure", task.exception)
                }
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("verification", verificationInProgress)
    }

    private fun addUserToDatabase(currentUser: FirebaseUser?) {
        val user = hashMapOf(
            "id" to currentUser?.uid.toString().trim(),
            "phone" to currentUser?.phoneNumber.toString().trim(),
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
        activity?.finish()
        startActivity(intent)
    }
}