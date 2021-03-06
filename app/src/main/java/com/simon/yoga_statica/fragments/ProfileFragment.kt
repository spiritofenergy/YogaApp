package com.simon.yoga_statica.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.simon.yoga_statica.R
import com.simon.yoga_statica.classes.User
import kotlinx.android.synthetic.main.fraagment_profile.view.*

@SuppressLint("UseSwitchCompatOrMaterialCode")
class ProfileFragment : Fragment() {

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val avatars: StorageReference = storage.reference

    private lateinit var auth: FirebaseAuth
    private var user = User()

    private lateinit var idUser: TextView
    private lateinit var emailUser: TextView
    private lateinit var nameUser: TextView
    private lateinit var status: TextView
    private lateinit var countAsuns: TextView
    private lateinit var phoneUser: TextView
    private lateinit var addAvatar: ImageButton

    private lateinit var imageAvatar: ImageView

    private lateinit var setTheme: RadioGroup
    private lateinit var setSecond: RadioGroup
    private lateinit var chooseDyh: RadioGroup
    private lateinit var chooseMusic: RadioGroup

    private lateinit var radio30: RadioButton
    private lateinit var radio60: RadioButton
    private lateinit var radio90: RadioButton

    private lateinit var radioDefault: RadioButton
    private lateinit var radioCoffee: RadioButton
    private lateinit var radioGreen: RadioButton
    private lateinit var radioRed: RadioButton
    private lateinit var radioOrange: RadioButton
    private lateinit var dyh1: RadioButton
    private lateinit var dyh2: RadioButton
    private lateinit var dyh3: RadioButton
    private lateinit var music1: RadioButton
    private lateinit var music2: RadioButton

    private lateinit var switchDyhSwitch: Switch
    private lateinit var simpleSwitchMusic: Switch
    private lateinit var simpleSwitchShava: Switch

    private lateinit var openDialog: Button

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"
    private val APP_PREFERENCES_COUNT = "count"
    private val APP_PREFERENCES_SHAVA = "shava"
    private val APP_PREFERENCES_DYH = "dyh"
    private val APP_PREFERENCES_MUSIC = "music"
    private val RESULT_IMAGE = 3214

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View = inflater.inflate(R.layout.fraagment_profile, container, false)

        activity?.title = getString(R.string.setting_profile)

        prefs = activity?.getSharedPreferences("settings", Context.MODE_PRIVATE)!!

        try {
            prefs.getBoolean(APP_PREFERENCES_SHAVA, true)
        } catch (e: ClassCastException) {
            prefs
                .edit()
                .putBoolean(APP_PREFERENCES_SHAVA, true)
                .apply()
        }

        auth = Firebase.auth

        idUser = rootView.findViewById(R.id.idUser)
        emailUser = rootView.findViewById(R.id.emailUser)
        phoneUser = rootView.phoneUser
        nameUser = rootView.findViewById(R.id.nameUser)
        status = rootView.findViewById(R.id.status)
        imageAvatar = rootView.findViewById(R.id.imageAvatar)

        radio30 = rootView.findViewById(R.id.radio30)
        radio60 = rootView.findViewById(R.id.radio60)
        radio90 = rootView.findViewById(R.id.radio90)

        radioDefault = rootView.findViewById(R.id.radioDefault)
        radioRed = rootView.findViewById(R.id.radioRed)
        radioOrange = rootView.findViewById(R.id.radioOrange)
        radioGreen = rootView.findViewById(R.id.radioLime)
        radioCoffee = rootView.findViewById(R.id.radioCoffee)

        chooseDyh = rootView.findViewById(R.id.Choose_duh)
        chooseMusic = rootView.findViewById(R.id.ChooseMusic)

        dyh1 = rootView.dyh_bhastrica
        dyh2 = rootView.dyh_capalaphaty
        dyh3 = rootView.dyh_udjay
        music1 = rootView.five_warm_up
        music2 = rootView.six_warm_up

        switchDyhSwitch = rootView.findViewById(R.id.simple_Chose_dyh)
        simpleSwitchMusic = rootView.findViewById(R.id.simpleSwitchMusic)
        simpleSwitchShava = rootView.findViewById(R.id.simpleSwitchShava)

          simpleSwitchShava.isChecked = prefs.getBoolean(APP_PREFERENCES_SHAVA, true)

        if (!prefs.contains(APP_PREFERENCES_DYH)) {
            switchDyhSwitch.isChecked = true
        } else {
            when (prefs.getString(APP_PREFERENCES_DYH, "dyh1")) {
                "off" -> {
                    switchDyhSwitch.isChecked = false
                }
                "dyh1" -> {
                    switchDyhSwitch.isChecked = true
                    dyh1.isChecked = true
                }
                "dyh2" -> {
                    switchDyhSwitch.isChecked = true
                    dyh2.isChecked = true
                }
                "dyh3" -> {
                    switchDyhSwitch.isChecked = true
                    dyh3.isChecked = true
                }
            }

        }

        dyh1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                prefs
                    .edit()
                    .putString(APP_PREFERENCES_DYH, "dyh1")
                    .apply()
            }
        }

        dyh2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                prefs
                    .edit()
                    .putString(APP_PREFERENCES_DYH, "dyh2")
                    .apply()
            }
        }

        dyh3.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                prefs
                    .edit()
                    .putString(APP_PREFERENCES_DYH, "dyh3")
                    .apply()
            }
        }

        openDialog = rootView.openDialog
        openDialog.setOnClickListener {
            val dialog = EditDialogFragment()
            dialog.activityOver = activity
            dialog.show(childFragmentManager, "edit_profile")
        }



        if (switchDyhSwitch.isChecked) {
            chooseDyh.visibility = View.VISIBLE
        } else {
            chooseDyh.visibility = View.GONE
        }
        if (simpleSwitchMusic.isChecked) {
            chooseMusic.visibility = View.VISIBLE
        } else {
            chooseMusic.visibility = View.GONE
        }

        switchDyhSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                chooseDyh.visibility = View.VISIBLE

                prefs
                    .edit()
                    .putString(APP_PREFERENCES_DYH, "dyh1")
                    .apply()
                dyh1.isChecked = true
            } else {


                AlertDialog.Builder(activity)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.off_title))
                    .setMessage(getString(R.string.confirm_off_breath))
                    .setPositiveButton(getString(R.string.confirm_title)) { dialog, which ->
                        chooseDyh.visibility = View.GONE
                        prefs
                            .edit()
                            .putString(APP_PREFERENCES_DYH, "off")
                            .apply()
                    }
                    .setNegativeButton(getString(R.string.cancel_title)) { dialogInterface: DialogInterface, i: Int ->
                        switchDyhSwitch.isChecked = true
                    }
                    .setOnCancelListener {
                        switchDyhSwitch.isChecked = true
                    }
                    .show()
            }
        }

        simpleSwitchMusic.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                prefs
                    .edit()
                    .putString(APP_PREFERENCES_MUSIC, "music1")
                    .apply()
                chooseMusic.visibility = View.VISIBLE
                music1.isChecked = true
            } else {
                chooseMusic.visibility = View.GONE
                prefs
                    .edit()
                    .putString(APP_PREFERENCES_MUSIC, "off")
                    .apply()
            }
        }

        simpleSwitchShava.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                prefs
                    .edit()
                    .putBoolean(APP_PREFERENCES_SHAVA, true)
                    .apply()
            } else {
                AlertDialog.Builder(activity)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.off_title))
                    .setMessage(getString(R.string.confirm_end_asana_off))
                    .setPositiveButton(getString(R.string.confirm_title)) { dialog, which ->
                        prefs
                            .edit()
                            .putBoolean(APP_PREFERENCES_SHAVA, false)
                            .apply()
                    }
                    .setNegativeButton(getString(R.string.cancel_title)) { dialogInterface: DialogInterface, i: Int ->
                        simpleSwitchShava.isChecked = true
                    }
                    .setOnCancelListener {
                        simpleSwitchShava.isChecked = true
                    }
                    .show()
            }
        }

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
                R.id.radioLime -> {
                    if (theme != "lime") {
                        set = "lime"
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

                intent?.putExtra("profile", true)

                activity?.overridePendingTransition(0, 0)
                startActivity(intent)

                activity?.finish()
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

        imageAvatar.setOnClickListener {
            getImage()
        }

        user.name = auth.currentUser?.displayName.toString()
        user.id = auth.currentUser?.uid.toString()
        user.email = auth.currentUser?.email
        user.phone = auth.currentUser?.phoneNumber
        user.photo = auth.currentUser?.photoUrl

        idUser.text = user.id
        emailUser.text = user.email
        phoneUser.text = user.phone

        if (user.email == "") {
            emailUser.visibility = View.GONE
        }
        if (user.phone == "") {
            phoneUser.visibility = View.GONE
        }
        nameUser.text = user.name

        val photo = user.photo
        if (photo == null) {
            imageAvatar.setImageResource(R.mipmap.ic_launcher)
        } else {
            openAvatar(photo)
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

        db.collection("users")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        user.status = (document["status"] as Long).toInt()
                        user.countAsuns = (document["countAsuns"] as Long).toInt()
                    }

                    countAsuns.text = user.countAsuns.toString()

                    status.text = when (user.status) {
                        1 -> getString(R.string.newer)
                        2 -> getString(R.string.middle)
                        3 -> getString(R.string.profi)
                        else -> getString(R.string.newer)
                    }

                    when (user.status) {
                        1 -> {
                            radio60.visibility = View.GONE
                            radio90.visibility = View.GONE
                        }
                        2 -> {
                            radio60.visibility = View.VISIBLE
                            radio90.visibility = View.GONE
                        }
                        3 -> {
                            radio60.visibility = View.VISIBLE
                            radio90.visibility = View.VISIBLE
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

                                if (isAdded) {
                                    openAvatar(downloadUri)
                                }

                                val profileUpdates = userProfileChangeRequest {
                                    photoUri = downloadUri
                                }
                                auth.currentUser!!.updateProfile(profileUpdates)
                            } else {
                                Toast.makeText(
                                    activity, getString(R.string.error_upload),
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
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.choose_image)),
            RESULT_IMAGE
        )
    }

    private fun openAvatar(downloadUri: Uri) {
        Glide.with(activity!!)
            .load(downloadUri)
            .listener(object : RequestListener<Drawable> {
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

                    return false
                }

            })
            .into(imageAvatar)
    }
}