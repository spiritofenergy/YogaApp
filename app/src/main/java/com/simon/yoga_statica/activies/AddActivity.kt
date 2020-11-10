package com.simon.yoga_statica.activies

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
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
    private var titleAsuna = ""
    private var shortDesc = ""
    private var longDesc = ""
    private var count = 0

    private var images: MutableList<MutableList<String>> = mutableListOf()
    private var elems: MutableList<HashMap<String, String>> = mutableListOf()
    private var imagesStr = ""
    private var imagesArray: MutableList<String> = mutableListOf("")
    private var curAsana = 0
    private var titles: MutableList<String> = mutableListOf()
    private var totalAll: HashMap<String, HashMap<String, Any>> = hashMapOf()
    private lateinit var frameButtonImageAdd: FrameLayout
    private lateinit var addYogaIconGrand: ImageView

    private var countOpen = 0
    private lateinit var newEdit: EditText


    private lateinit var addTitle: TextView
    private lateinit var addShortAsuns: TextView
    private lateinit var addLongAsuns: TextView
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

        addTitle = findViewById(R.id.addAsanaTitle)
        addAsuns = findViewById(R.id.addAsuns)
        addImage = findViewById(R.id.addPhoto)
        addedImage = findViewById(R.id.addedImage)
        frameButtonImageAdd = findViewById(R.id.frameButtonImageAdd)

        addShortAsuns = findViewById(R.id.addShortDescription)
        addLongAsuns = findViewById(R.id.ddLongDescription)
        addYogaIconGrand = findViewById(R.id.addYogaIconGrand)

        images.add(mutableListOf())

        addAsuns.background = ContextCompat.getDrawable(
            this,
            resources.getIdentifier(
                "inset_button_$theme",
                "drawable",
                packageName
            )
        )

        addTitle.setOnClickListener {
            openDialog("Add Title", "Please, add title",
                { _, _ ->
                    if (newEdit.text.toString() != "") {
                        titleAsuna = newEdit.text.toString()
                        addTitle.text = titleAsuna
                        elems[0]["title"] = titleAsuna
                        edit = true

                        if (Build.VERSION.SDK_INT >= 23)
                            addTitle.setTextColor(getColor(R.color.colorTextTitle))
                        else
                            addTitle.setTextColor(resources.getColor(R.color.colorTextTitle))
                    } else
                        if (titleAsuna == "")
                            addTitle.setTextColor(Color.RED)
                }, { _, _ ->
                    if (titleAsuna == "")
                        addTitle.setTextColor(Color.RED)
                }, {
                    if (titleAsuna == "")
                        addTitle.setTextColor(Color.RED)
                }, default = titleAsuna)
        }

        addShortAsuns.setOnClickListener {
            openDialog("Add Short Description", "Please, add short description",
                { _, _ ->
                    if (newEdit.text.toString() != "") {
                        shortDesc = newEdit.text.toString()
                        addShortAsuns.text = shortDesc
                        elems[0]["shortDescription"] = shortDesc
                        edit = true

                        if (Build.VERSION.SDK_INT >= 23)
                            addShortAsuns.setTextColor(getColor(R.color.colorTextTitle))
                        else
                            addShortAsuns.setTextColor(resources.getColor(R.color.colorTextTitle))
                    } else
                        if (shortDesc == "")
                            addShortAsuns.setTextColor(Color.RED)
                }, { _, _ ->
                    if (shortDesc == "")
                        addShortAsuns.setTextColor(Color.RED)
                }, {
                    if (shortDesc == "")
                        addShortAsuns.setTextColor(Color.RED)
                }, true, shortDesc)
        }

        addLongAsuns.setOnClickListener {
            openDialog("Add Long Description", "Please, add long description",
                { _, _ ->
                    if (newEdit.text.toString() != "") {
                        longDesc = newEdit.text.toString()
                        addLongAsuns.text = longDesc
                        elems[0]["description"] = longDesc
                        edit = true

                        if (Build.VERSION.SDK_INT >= 23)
                            addLongAsuns.setTextColor(getColor(R.color.colorTextTitle))
                        else
                            addLongAsuns.setTextColor(resources.getColor(R.color.colorTextTitle))
                    } else
                        if (longDesc == "")
                            addLongAsuns.setTextColor(Color.RED)
                }, { _, _ ->
                    if (longDesc == "")
                        addLongAsuns.setTextColor(Color.RED)
                }, {
                    if (longDesc == "")
                        addLongAsuns.setTextColor(Color.RED)
                }, true, longDesc)
        }

        getCountAsuns()

        addImage.setOnClickListener {
            edit = true
            getImage()
        }

        addAsuns.setOnClickListener {
            AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.additional_asana_title))
                .setMessage(getString(R.string.confirm_adational_asana))
                .setPositiveButton(getString(R.string.ending)) { _, _ ->
                    totalAll = getMapOfData()

                    Log.d("map", totalAll.toString())

                    if (totalAll.isEmpty()) {
                        Toast.makeText(
                            this, getString(R.string.put_all_fields),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        addAsunaInFire()
                    }

                }
                .setNegativeButton(getString(R.string.no)) { _, _ ->
                }
                .show()
        }
//
//        addNewAsuna.setOnClickListener {
//            edit = true
//            imagesArray.add("")
//            images.add(mutableListOf())
//            countOpen++
//            addNew()
//        }

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
                .setTitle(getString(R.string.end_adidition))
                .setMessage(getString(R.string.confirm_end_addition))
                .setPositiveButton(getString(R.string.ending)) { _, _ ->
                    for (indexing in 0 until curAsana + 1) {
                        for (item in images[indexing]) {
                            Log.d("testDel", item)
                            avatars.child("thumbnails/$item.jpeg").delete()
                        }
                    }
                    super.onBackPressed()
                }
                .setNegativeButton(getString(R.string.no)) { _, _ ->
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

        rowView.tag = getString(R.string.open)
        rowView.findViewById<ImageButton>(R.id.addImageOpen).setOnClickListener {
            curAsana = it.tag as Int

            edit = true
            getImage()
        }

        titles.add("open_asana${count}_$countOpen")
        rowView.findViewById<ImageButton>(R.id.addImageOpen).tag = countOpen

        elems.add(
            hashMapOf(
                "title" to rowView.findViewById(R.id.addTitleOpen),
                "description" to rowView.findViewById(R.id.addLongAsunsOpen)
            )
        )

        Log.d("elems", elems.toString())
    }

    private fun getCountAsuns() {
        db.collection("asunaRU")
            .get()
            .addOnSuccessListener { result ->
                count = result.size() + 1
                titles.add("asuna${count}")
                elems.add(hashMapOf())
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

                                Glide.with(this)
                                    .load(downloadUri)
                                    .into(addYogaIconGrand)

                                addedImage.adapter = SliderAdapter(images[curAsana])
                                frameButtonImageAdd.visibility = View.GONE
                                addedImage.visibility = View.VISIBLE
                            } else {
                                Toast.makeText(
                                    this, getString(R.string.error_upload) + task.exception,
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

                                        if (images[curAsana][0] == nameImg) {
                                            Glide.with(this)
                                                .load(downloadUri)
                                                .into(addYogaIconGrand)
                                        }

                                        addedImage.adapter = SliderAdapter(images[curAsana])
                                        frameButtonImageAdd.visibility = View.GONE
                                        addedImage.visibility = View.VISIBLE
                                    } else {
                                        Toast.makeText(
                                            this, getString(R.string.error_upload) + task.exception,
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

            val titleAsana = elems[index]["title"].toString()

            var shortDescription = ""
            if (index == 0) {
                shortDescription = elems[index]["shortDescription"].toString()
            }
            val description = elems[index]["description"].toString()

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
                    "description" to description
                )
                if (opens != "") {
                    hashMap[title]?.set("openAsans", opens)
                }
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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun openDialog(
        title: String,
        message: String,
        listener: DialogInterface.OnClickListener,
        listenerNegative: DialogInterface.OnClickListener,
        onCancel: DialogInterface.OnCancelListener,
        multiline: Boolean = false,
        default: String? = null
    ) {
        val alert = AlertDialog.Builder(this)

        alert.setTitle(title)
        alert.setMessage(message)

        newEdit = EditText(this)
        newEdit.isSingleLine = false
        if (multiline)
            newEdit.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        if (default != null)
            newEdit.append(default)
        alert.setView(newEdit)

        alert.setPositiveButton("Ok", listener)

        alert.setNegativeButton("Cancel", listenerNegative)

        alert.setOnCancelListener(onCancel)

        alert.show()
    }

}