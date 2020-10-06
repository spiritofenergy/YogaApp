package com.simon.yoga_statica.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.simon.yoga_statica.R
import com.simon.yoga_statica.classes.User

class ProfileFragment : Fragment() {
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val avatars: StorageReference = storage.reference

    private lateinit var auth: FirebaseAuth
    private var user = User()

    private lateinit var nameUser: TextView
    private lateinit var status: TextView
    private lateinit var countAsuns: TextView
    private lateinit var addAvatar: ImageButton

    private lateinit var imageAvatar: ImageView

    private lateinit var setTheme: RadioGroup
    private lateinit var setSecond: RadioGroup

    private lateinit var radio30: RadioButton
    private lateinit var radio60: RadioButton
    private lateinit var radio90: RadioButton

    private lateinit var radioDefault: RadioButton
    private lateinit var radioCoffee: RadioButton
    private lateinit var radioGreen: RadioButton
    private lateinit var radioRed: RadioButton
    private lateinit var radioOrange: RadioButton

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"
    private val APP_PREFERENCES_COUNT = "count"
    private val RESULT_IMAGE = 3214

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
          val rootView: View = inflater.inflate(R.layout.fraagment_profile, container, false)

        prefs = activity?.getSharedPreferences("settings", Context.MODE_PRIVATE)!!

        auth = Firebase.auth

        nameUser = rootView.findViewById(R.id.nameUser)
        status = rootView.findViewById(R.id.status)
        addAvatar = rootView.findViewById(R.id.addAvatar)
        imageAvatar = rootView.findViewById(R.id.imageAvatar)

        radio30 = rootView.findViewById(R.id.radio30)
        radio60 = rootView.findViewById(R.id.radio60)
        radio90 = rootView.findViewById(R.id.radio90)

        radioDefault = rootView.findViewById(R.id.radioDefault)
        radioRed = rootView.findViewById(R.id.radioRed)
        radioOrange = rootView.findViewById(R.id.radioOrange)
        radioGreen = rootView.findViewById(R.id.radioGreen)
        radioCoffee = rootView.findViewById(R.id.radioCoffee)



        setTheme = rootView.findViewById(R.id.setThemeGroup)
        setSecond = rootView.findViewById(R.id.setSecond)
        countAsuns = rootView.findViewById(R.id.countAsuns)

        setTheme.setOnCheckedChangeListener { _, id ->
            var theme = "default"
            var set = ""
            if (prefs.contains(APP_PREFERENCES_THEME)) {
                theme = prefs.getString(APP_PREFERENCES_THEME, "default").toString()
            }

            when(id) {
                R.id.radioDefault -> {
                    if (theme != "default") {
                        set = "default"
                    }
                }
                R.id.radioRed -> {
                    if (theme != "red") {
                        set = "red"
                    }
                }
                R.id.radioOrange -> {
                    if (theme != "orange") {
                        set = "orange"
                    }
                }
                R.id.radioGreen -> {
                    if (theme != "green") {
                        set = "green"
                    }
                }
                R.id.radioCoffee -> {
                    if (theme != "coffee") {
                        set = "coffee"
                    }
                }
            }

            if (set != "") {
                prefs
                    .edit()
                    .putString(APP_PREFERENCES_THEME, set)
                    .apply()

                val intent = activity?.intent
                intent?.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
                activity?.overridePendingTransition(0, 0)
                activity?.finish()

                intent?.putExtra("profile", true)

                activity?.overridePendingTransition(0, 0)
                startActivity(intent)
            }
        }

        setSecond.setOnCheckedChangeListener { _, id ->

            val sec = when(id) {
                R.id.radio30 -> 30
                R.id.radio60 -> 60
                R.id.radio90 -> 90
                else -> 30
            }


            prefs
                .edit()
                .putInt(APP_PREFERENCES_COUNT, sec)
                .apply()
        }

        addAvatar.setOnClickListener {
            getImage()
        }

        db.collection("users")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        user.name = document["name"].toString()
                        user.id = document["id"].toString()
                        user.email = document["email"].toString()
                        user.status = (document["status"] as Long).toInt()
                        user.sec = (document["sec"] as Long).toInt()
                        user.colorTheme = document["colorTheme"].toString()
                        user.countAsuns = (document["countAsuns"] as Long).toInt()
                        user.photo = document["photo"].toString()
                    }

                    nameUser.text = user.name
                    countAsuns.text = user.countAsuns.toString()

                    if (user.photo == "") {
                        imageAvatar.setImageResource(R.mipmap.ic_launcher)
                        addAvatar.visibility = View.VISIBLE
                    } else {
                        val downloadUri: Uri = Uri.parse(user.photo)
                        openAvatar(downloadUri)
                        imageAvatar.setOnClickListener {
                            getImage()
                        }
                    }

                    Log.d("email", user.email)
                    Log.d("st", user.status.toString())

                    status.text = when (user.status) {
                        1 -> "Новичок"
                        2 -> ""
                        3 -> ""
                        else -> ""
                    }

                    when (user.status) {
                        2 -> radio60.visibility = View.VISIBLE
                        3 -> {
                            radio60.visibility = View.VISIBLE
                            radio90.visibility = View.VISIBLE
                        }
                    }

                    if (!prefs.contains(APP_PREFERENCES_COUNT)) {
                        radio30.isChecked = true
                    } else {
                        when (prefs.getInt(APP_PREFERENCES_COUNT, 30)) {
                            30 -> radio30.isChecked = true
                            60 -> radio60.isChecked = true
                            90 -> radio90.isChecked = true
                        }
                    }

                    if (!prefs.contains(APP_PREFERENCES_THEME)) {
                        radioDefault.isChecked = true
                    } else {
                        when (prefs.getString(APP_PREFERENCES_THEME, "default")) {

                            "default" -> radioDefault.isChecked = true
                            "red" -> radioRed.isChecked = true
                            "orange" -> radioOrange.isChecked = true
                            "green" -> radioGreen.isChecked = true
                            "coffee" -> radioCoffee.isChecked = true
                        }
                    }
                }

            }
            .addOnFailureListener { exception ->
                Log.w("home", "Error getting documents: ", exception)
            }

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RESULT_IMAGE -> {
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        Log.d("uri", selectedImageUri.lastPathSegment.toString())
                        val upload: UploadTask = avatars
                            .child("avatars/${auth.currentUser?.uid}")
                            .putFile(selectedImageUri)

                        val urlTask = upload.continueWithTask { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    throw it
                                }
                            }
                            avatars.child("avatars/${auth.currentUser?.uid}").downloadUrl
                        }.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val downloadUri = task.result

                                openAvatar(downloadUri)

                                db.collection("users")
                                    .whereEqualTo("id", auth.currentUser?.uid)
                                    .get()
                                    .addOnSuccessListener { documents ->
                                        if (!documents.isEmpty) {
                                            for (document in documents) {
                                                db.collection("users")
                                                    .document(document.id)
                                                    .update("photo", downloadUri.toString())
                                            }


                                        }

                                    }
                                    .addOnFailureListener { exception ->
                                        Log.w("home", "Error getting documents: ", exception)
                                    }

                            } else {
                                Toast.makeText(
                                    activity, "Upload image failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(Intent.createChooser(intent, "Выберете изображение"), RESULT_IMAGE)
    }

    private fun openAvatar(downloadUri: Uri) {
        Glide.with(activity!!)
            .load(downloadUri)
            .listener( object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    addAvatar.visibility = View.GONE

                    return false
                }

            })
            .into(imageAvatar)
    }
}