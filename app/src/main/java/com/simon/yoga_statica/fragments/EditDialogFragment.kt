package com.simon.yoga_statica.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*

class EditDialogFragment : DialogFragment() {

    private lateinit var auth: FirebaseAuth

    private lateinit var nameEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var emailEdit: EditText

    private lateinit var saveEditProfile: Button

    var activityOver: FragmentActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        auth = Firebase.auth

        nameEdit = rootView.userNameEdit
        nameEdit.append(auth.currentUser?.displayName)

        phoneEdit = rootView.userPhoneEdit
        if (auth.currentUser?.phoneNumber != null)
            phoneEdit.append(auth.currentUser?.phoneNumber)

        emailEdit = rootView.userEmailEdit
        emailEdit.append(auth.currentUser?.email)

        saveEditProfile = rootView.saveEditProfile
        saveEditProfile.setOnClickListener {
            val profileUpdates = userProfileChangeRequest {
                displayName = nameEdit.text.toString()
            }
            if (emailEdit.text.toString() != "")
                auth.currentUser!!
                    .updateEmail(emailEdit.text.toString())
            auth.currentUser!!
                .updateProfile(profileUpdates)
                .addOnCompleteListener {
                    dismiss()

                    val intent = activityOver?.intent
                    intent?.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                or Intent.FLAG_ACTIVITY_NO_ANIMATION
                    )

                    intent?.putExtra("profile", true)

                    activityOver?.overridePendingTransition(0, 0)
                    startActivity(intent)

                    activityOver?.finish()
                }
        }


        return rootView
    }
}