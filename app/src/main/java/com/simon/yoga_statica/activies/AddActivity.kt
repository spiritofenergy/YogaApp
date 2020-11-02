package com.simon.yoga_statica.activies

import android.annotation.SuppressLint
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
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.simon.yoga_statica.R
import com.simon.yoga_statica.adapters.SliderAdapter

class AddActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val avatars: StorageReference = storage.reference

    private var edit = false
    private var count = 0

    private var images: MutableList< MutableList<String> > = mutableListOf()
    private var elems: MutableList< HashMap< String, EditText > > = mutableListOf()
    private var imagesStr = ""
    private var imagesArray: MutableList<String> = mutableListOf("")
    private var curAsana = 0
    private var titles: MutableList<String> = mutableListOf()
    private var totalAll: HashMap<String, HashMap<String, Any>> = hashMapOf()
    private var imagesPreviewIDs: MutableList<ViewPager2> = mutableListOf()

    private var countOpen = 0


    private lateinit var addTitle: EditText
    private lateinit var addShortAsuns: EditText
    private lateinit var addLongAsuns: EditText
    private lateinit var addImage: ImageButton
    private lateinit var addAsuns: Button
    private lateinit var addNewAsuna: Button
    private lateinit var layoutParent: LinearLayout

    private lateinit var addedImage: ViewPager2

    private lateinit var auth: FirebaseAuth

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"

    private val RESULT_IMAGE = 3214

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Добавление асуны"

        auth = Firebase.auth

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)

        val theme = prefs.getString(APP_PREFERENCES_THEME, "default")

        when (theme) {
            "default" -> setTheme(R.style.AppTheme)
            "red" -> setTheme(R.style.RedAppTheme)
            "orange" -> setTheme(R.style.OrangeAppTheme)
            "lime" -> setTheme(R.style.LimeAppTheme)
            "coffee" -> setTheme(R.style.CoffeeAppTheme)
        }
        setContentView(R.layout.activity_add)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        addTitle = findViewById(R.id.addTitle)
        addShortAsuns = findViewById(R.id.addShortAsuns)
        addLongAsuns = findViewById(R.id.addLongAsuns)
        addImage = findViewById(R.id.addImage)
        addAsuns = findViewById(R.id.addAsuns)
        addNewAsuna = findViewById(R.id.addNewAsuna)
        addedImage = findViewById(R.id.addedImage)
        layoutParent = findViewById(R.id.layoutParent)
        addAsuns.background = ContextCompat.getDrawable(
            this,
            resources.getIdentifier(
                "inset_button_$theme",
                "drawable",
                packageName
            )
        )

        elems.add(hashMapOf(
            "title" to addTitle,
            "shortDescription" to addShortAsuns,
            "description" to addLongAsuns
        ))
        images.add(mutableListOf())

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
            curAsana = 0
            getImage(addedImage)
        }

        addAsuns.setOnClickListener {
            AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Добавление асaны")
                .setMessage("Вы уверены, что хотите завершить редактирование?")
                .setPositiveButton("Завершить") { _, _ ->
                    totalAll = getMapOfData()

                    Log.d("map", totalAll.toString())

                    if (totalAll.isEmpty()) {
                        Toast.makeText(
                            this, "Заполните все поля",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        addAsunaInFire()
                    }

                }
                .setNegativeButton("Нет") { _, _ ->
                }
                .show()
        }

        addNewAsuna.setOnClickListener {
            edit = true
            imagesArray.add("")
            images.add(mutableListOf())
            countOpen++
            addNew()
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
                    for (item in images) {
                        avatars.child("thumbnails/$item.jpeg").delete()
                    }
                    super.onBackPressed()
                }
                .setNegativeButton("Нет") { _, _ ->
                }
                .show()
        } else {
            super.onBackPressed()
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("InflateParams")
    private fun addNew() {
        val inflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView: View = inflater.inflate(R.layout.add_new_item, null)
        layoutParent.addView(rowView, layoutParent.childCount)

        rowView.tag = "Open"
        rowView.findViewById<ImageButton>(R.id.addImageOpen).setOnClickListener {
            curAsana = it.tag as Int

            edit = true
            getImage(imagesPreviewIDs[curAsana - 1])
        }

        titles.add("open_asana${count}_$countOpen")
        rowView.findViewById<ImageButton>(R.id.addImageOpen).tag = countOpen

        imagesPreviewIDs.add(rowView.findViewById(R.id.addedImageOpen))
        Log.d("prew", imagesPreviewIDs.toString())
        elems.add(hashMapOf(
            "title" to rowView.findViewById(R.id.addTitleOpen),
            "description" to rowView.findViewById(R.id.addLongAsunsOpen)
        ))

        Log.d("elems", elems.toString())
    }

    private fun getCountAsuns() {
        db.collection("asunaRU")
            .get()
            .addOnSuccessListener { result ->
                count = result.size() + 1
                titles.add("asuna${count}")
            }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun getImage(pager2: ViewPager2) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        addedImage = pager2
        startActivityForResult(Intent.createChooser(intent, "Выберете изображение"), RESULT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RESULT_IMAGE -> {
                    for (item in images[curAsana]) {
                        avatars.child("thumbnails/$item.jpeg").delete()
                    }
                    images[curAsana].clear()
                    imagesArray[curAsana] = ""
                    val selectedImageUri: Uri? = data?.data

                    Log.d("images", images.toString())

                    if (selectedImageUri != null) {
                        val nameImg = getRandomString()
                        val upload: UploadTask = avatars
                            .child("thumbnails/$nameImg.jpeg")
                            .putFile(selectedImageUri)

                        images[curAsana].add(nameImg)
                        imagesStr = images[curAsana].joinToString(" ")

                        imagesArray[curAsana] = imagesStr

                        Log.d("as", imagesArray.toString())

                        Log.d("imagesOne", "true")
                        val urlTask = upload.continueWithTask { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    throw it
                                }
                            }
                            avatars.child("thumbnails/$nameImg.jpeg").downloadUrl
                        }.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val downloadUri = task.result

                                addedImage.adapter = SliderAdapter(images[curAsana])
                            } else {
                                Toast.makeText(
                                    this, "Upload image failed. ${task.exception}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    } else {
                        val clipData: ClipData? = data?.clipData

                        if (clipData != null) {
                            Log.d("imagesNoNe", "true")

                            for (i in 0 until clipData.itemCount) {
                                val nameImg = getRandomString()
                                val upload: UploadTask = avatars
                                    .child("thumbnails/$nameImg.jpeg")
                                    .putFile(clipData.getItemAt(i).uri)

                                images[curAsana].add(nameImg)

                                Log.d("as", imagesArray.toString())

                                val urlTask = upload.continueWithTask { task ->
                                    if (!task.isSuccessful) {
                                        task.exception?.let {
                                            throw it
                                        }
                                    }
                                    avatars.child("thumbnails/$nameImg.jpeg").downloadUrl
                                }.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val downloadUri = task.result

                                        addedImage.adapter = SliderAdapter(images[curAsana])
                                    } else {
                                        Toast.makeText(
                                            this, "Upload image failed. ${task.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                            imagesStr = images[curAsana].joinToString(" ")
                            imagesArray[curAsana] = imagesStr
                        }
                    }
                }
            }
        }
    }

    private fun getRandomString() : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..28)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun getMapOfData() : HashMap<String, HashMap<String, Any>> {
        val hashMap: HashMap<String, HashMap<String, Any>> = hashMapOf()
        val iterator = titles.iterator()
        for ((index, title) in iterator.withIndex()) {

            val titleAsana = elems[index]["title"]
                ?.text
                .toString()
            var shortDescription = ""
            if (index == 0) {
                shortDescription = elems[index]["shortDescription"]
                    ?.text
                    .toString()
            }
            val description = elems[index]["description"]
                ?.text
                .toString()

            if (titleAsana == "" || (shortDescription == "" && index == 0) || description == "" || imagesArray[index] == "") {
                hashMap.clear()
                return hashMap
            }

            val opens = titles.drop(1).joinToString(" ")

            if (index == 0) {
                hashMap[title] = hashMapOf(
                    "comments" to 0,
                    "likes" to 0,
                    "thumbPath" to imagesArray[index],
                    "title" to titleAsana,
                    "shortDescription" to shortDescription,
                    "description" to description,
                    "openAsans" to opens
                )
            } else {
                hashMap[title] = hashMapOf(
                    "thumbPath" to imagesArray[index],
                    "title" to titleAsana,
                    "description" to description
                )
            }
        }

        return hashMap
    }

    private fun addAsunaInFire() {
        for ((title, asuna) in totalAll) {
            val ref: CollectionReference
            if (title.indexOf("open") != -1) {
                ref = db.collection("openAsunaRU")
            } else {
                ref = db.collection("asunaRU")
            }
            ref
                .document(title)
                .set(asuna)
                .addOnSuccessListener {
                    val intent = Intent(
                        this,
                        MainActivity::class.java
                    )
                    startActivity(intent)
                }
        }
    }

}