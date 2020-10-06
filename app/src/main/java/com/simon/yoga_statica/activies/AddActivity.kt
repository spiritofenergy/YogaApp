package com.simon.yoga_statica.activies

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
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
import com.simon.yoga_statica.fragments.FavoriteListFragment

class AddActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val avatars: StorageReference = storage.reference

    private var edit = false
    private var count = 0

    private lateinit var addTitle: EditText
    private lateinit var addShortAsuns: EditText
    private lateinit var addLongAsuns: EditText
    private lateinit var addImage: ImageView
    private lateinit var addAsuns: Button

    private lateinit var auth: FirebaseAuth

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"

    private val RESULT_IMAGE = 3214

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("Добавление асуны")

        auth = Firebase.auth

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)

        if (!prefs.contains(APP_PREFERENCES_THEME) || auth.currentUser == null) {
            setTheme(R.style.AppTheme)
        } else {
            when (prefs.getString(APP_PREFERENCES_THEME, "default")) {
                "coffee" -> setTheme(R.style.CoffeeAppTheme)
                "default" -> setTheme(R.style.AppTheme)
            }
        }
        setContentView(R.layout.activity_add)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        addTitle = findViewById(R.id.addTitle)
        addShortAsuns = findViewById(R.id.addShortAsuns)
        addLongAsuns = findViewById(R.id.addLongAsuns)
        addImage = findViewById(R.id.addImage)
        addAsuns = findViewById(R.id.addAsuns)

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
            addAsunaInFire()
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

    private fun getImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(Intent.createChooser(intent, "Выберете изображение"), RESULT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RESULT_IMAGE -> {
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        val upload: UploadTask = avatars
                            .child("thumbnails/asuna${count}.jpeg")
                            .putFile(selectedImageUri)

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
                                    this, "Upload image failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openAvatar(downloadUri: Uri) {
        Glide.with(this)
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
                    addImage.scaleType = ImageView.ScaleType.FIT_CENTER

                    return false
                }

            })
            .into(addImage)
    }

    private fun addAsunaInFire() {
        db.collection("asunaRU")
            .document("asuna${count}")
            .set(hashMapOf(
                "comments" to 0,
                "likes" to 0,
                "thumbPath" to "asuna${count}",
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

//Триконасана

//Поза треугольника довольно-таки сложная асана для тех, у кого с гибкостью всё печально.
//И хороша для остальных — развивает баланс, растягивает подколенные сухожилия и внутреннюю поверхность бёдер.
//Как сделать: стойте прямо, ноги расставьте широко.
//Правая ступня «смотрит» вперёд, левая — в сторону, в которую направлен ваш корпус.
//В таком положении таз слегка развернётся. Обе ноги должны быть прямыми.
//Поднимите руки (но не плечи) в стороны так, чтобы они образовали одну прямую линию, параллельную полу.
//Тяните правую ладонь, а за ней и корпус вперёд, будто вас кто-то тянет к стене.
//Как только поймёте, что тянуться дальше не можете, опустите руку вниз и, если сможете,         поставьте ладонь на пол впереди или сзади ноги. Если нет, держите руку на ноге.
//Сохраняйте прямую линию в руках и смотрите вверх, на левую ладонь.
//Вес распределите равномерно на обе ноги, а в корпусе не допускайте прогиба.
//Дышите глубоко и спокойно.