package com.simon.yoga_statica.activies

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.simon.yoga_statica.R

class AddActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val avatars: StorageReference = storage.reference

    private var edit = false
    private var photo = false
    private var count = 0

    private var images = ""

    private lateinit var addTitle: EditText
    private lateinit var addShortAsuns: EditText
    private lateinit var addLongAsuns: EditText
    private lateinit var addImage: ImageButton
    private lateinit var addAsuns: Button

    private lateinit var auth: FirebaseAuth

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"

    private val RESULT_IMAGE = 3214

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("Добавление асуны")

        auth = Firebase.auth

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)

        val theme = if (!prefs.contains(APP_PREFERENCES_THEME) || auth.currentUser == null) {
            "default"
        } else {
            when (prefs.getString(APP_PREFERENCES_THEME, "default")) {

                "default" -> "default"
                "red"    -> "red"
                "orange" -> "orange"
                "green"  -> "green"
                "coffee" -> "coffee"
                else     -> "default"
            }
        }

        when (theme) {
            "default" -> setTheme(R.style.AppTheme)
            "red" -> setTheme(R.style.RedAppTheme)
            "orange" -> setTheme(R.style.OrangeAppTheme)
            "green" -> setTheme(R.style.GreenAppTheme)
            "coffee" -> setTheme(R.style.CoffeeAppTheme)
        }
        setContentView(R.layout.activity_add)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        addTitle = findViewById(R.id.addTitle)
        addShortAsuns = findViewById(R.id.addShortAsuns)
        addLongAsuns = findViewById(R.id.addLongAsuns)
        addImage = findViewById(R.id.addImage)
        addAsuns = findViewById(R.id.addAsuns)
        addAsuns.background = ContextCompat.getDrawable(
            this,
            resources.getIdentifier(
                "inset_button_$theme",
                "drawable",
                packageName
            )
        )

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                edit = true
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        }

        addTitle.addTextChangedListener(textWatcher)
        addShortAsuns.addTextChangedListener(textWatcher)
        addLongAsuns.addTextChangedListener(textWatcher)

        getCountAsuns()

        addImage.setOnClickListener {
            edit = true

            getImage()
        }

        addAsuns.setOnClickListener {
            AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Добавление асaны")
                .setMessage("Вы уверены, что хотите завершить редактирование?")
                .setPositiveButton("Завершить") { _, _ ->
                    if (edit && addTitle.text.isNotEmpty() && addLongAsuns.text.isNotEmpty() && addShortAsuns.text.isNotEmpty())
                        addAsunaInFire()
                    else
                        Toast.makeText(
                            this, "Заполните все поля",
                            Toast.LENGTH_SHORT
                        ).show()
                }
                .setNegativeButton("Нет") { _, _ ->
                }
                .show()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {

        if (edit) {
            AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Завершение добавления асуны")
                .setMessage("Вы уверены, что хотите завершить добавление?")
                .setPositiveButton("Завершить") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("Нет") { _, _ ->
                }
                .show()
        } else {
            super.onBackPressed()
        }
    }

    private fun getCountAsuns() {
        db.collection("asunaRU")
            .get()
            .addOnSuccessListener { result ->
                count = result.size() + 1
            }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun getImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(Intent.createChooser(intent, "Выберете изображение"), RESULT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RESULT_IMAGE -> {
                    val selectedImageUri: Uri? = data?.data

                    Log.d("images", images)

                    if (selectedImageUri != null) {
                        val upload: UploadTask = avatars
                            .child("thumbnails/asuna${count}.jpeg")
                            .putFile(selectedImageUri)
                        Log.d("imagesOne", "true")
                        val urlTask = upload.continueWithTask { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    throw it
                                }
                            }
                            avatars.child("thumbnails/asuna${count}.jpeg").downloadUrl
                        }.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val downloadUri = task.result
                                photo = true

                                images = "asuna${count}"
                            } else {
                                Toast.makeText(
                                    this, "Upload image failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        val clipData: ClipData? = data?.clipData

                        if (clipData != null) {
                            Log.d("imagesNoNe", "true")
                            // Add to Array And Storage
                            // Add Delete
                            // If delete -> Delete from Storage
                            // Add New ImageViews By Array
                            for (i in 0 until clipData.itemCount) {
                                val upload: UploadTask = avatars
                                    .child("thumbnails/asuna${count}_${i}.jpeg")
                                    .putFile(clipData.getItemAt(i).uri)

                                val urlTask = upload.continueWithTask { task ->
                                    if (!task.isSuccessful) {
                                        task.exception?.let {
                                            throw it
                                        }
                                    }
                                    avatars.child("thumbnails/asuna${count}.jpeg").downloadUrl
                                }.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val downloadUri = task.result
                                        photo = true

                                        images += "asuna${count}_${i} "
                                        Log.d("images", images)
                                    } else {
                                        Toast.makeText(
                                            this, "Upload image failed.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                Log.d("images", images)
                            }
                        }
                    }
                }
            }
        }
    }

//    private fun openAvatar(downloadUri: Uri) {
//        Glide.with(this)
//            .load(downloadUri)
//            .listener( object : RequestListener<Drawable> {
//                override fun onLoadFailed(
//                    e: GlideException?,
//                    model: Any?,
//                    target: Target<Drawable>?,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    return false
//                }
//
//                override fun onResourceReady(
//                    resource: Drawable?,
//                    model: Any?,
//                    target: Target<Drawable>?,
//                    dataSource: DataSource?,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    addImage.scaleType = ImageView.ScaleType.FIT_CENTER
//
//                    return false
//                }
//
//            })
//            .into(addImage)
//    }

    private fun addAsunaInFire() {
        db.collection("asunaRU")
            .document("asuna${count}")
            .set(hashMapOf(
                "comments" to 0,
                "likes" to 0,
                "thumbPath" to images,
                "title" to addTitle.text.toString(),
                "shortDescription" to addShortAsuns.text.toString(),
                "description" to addLongAsuns.text.toString()
            ))
            .addOnSuccessListener {
                val intent = Intent(
                    this,
                    MainActivity::class.java
                )
                startActivity(intent)
            }
    }

}