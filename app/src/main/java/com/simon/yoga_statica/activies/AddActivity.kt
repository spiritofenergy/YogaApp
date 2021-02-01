package com.simon.yoga_statica.activies

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MarginLayoutParamsCompat
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.google.common.primitives.UnsignedBytes
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
import com.simon.yoga_statica.classes.EditTextDialog
import com.simon.yoga_statica.interfaces.OnRecyclerItemClickListener
import com.simon.yoga_statica.interfaces.OnSubmitDialog

/**
 * Класс активити добавления новой асаны
 *
 */
class AddActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val avatars: StorageReference = storage.reference
    private val thumbnails: StorageReference = storage.reference.child("thumbnails")

    private var edit = false
    private var titleAsuna: String? = null
    private var shortDesc: String? = null
    private var longDesc: String? = null
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

    private var countOpenNew = 0

    private lateinit var addTitle: TextView
    private lateinit var addShortAsuns: TextView
    private lateinit var addLongAsuns: TextView
    private lateinit var addImage: ImageButton
    private lateinit var addAsuns: Button
    private lateinit var addNewAsuna: ImageView
    private lateinit var openAsansOpen: LinearLayout

    private lateinit var addedImage: ViewPager2

    private lateinit var auth: FirebaseAuth

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"

    private val RESULT_IMAGE = 3214

    /**
     * Метод создания активити
     *
     * @param savedInstanceState    Сохраненные данные при перезагрузке активити
     */
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
        addNewAsuna = findViewById(R.id.addNewOpen)
        openAsansOpen = findViewById(R.id.openAsansOpen)

        addShortAsuns = findViewById(R.id.addShortDescription)
        addLongAsuns = findViewById(R.id.ddLongDescription)

        addedImage = findViewById(R.id.addedImage)
        frameButtonImageAdd = findViewById(R.id.frameButtonImageAdd)
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
            EditTextDialog(
                this,
                addTitle,
                "Add Title", "Please, add title",
                titleAsuna,
                false,
                object : OnSubmitDialog {
                    override fun setOnClickPositive(
                        view: EditText,
                        dialog: DialogInterface,
                        which: Int
                    ) {
                        titleAsuna = view.text.toString()
                        addTitle.text = titleAsuna
                        elems[0]["title"] = titleAsuna!!
                        edit = true
                    }

                }
            ).show()
        }

        addShortAsuns.setOnClickListener {
            EditTextDialog(
                this,
                addShortAsuns,
                "Add Short Description", "Please, add short description",
                shortDesc,
                true,
                object : OnSubmitDialog {
                    override fun setOnClickPositive(
                        view: EditText,
                        dialog: DialogInterface,
                        which: Int
                    ) {
                        shortDesc = view.text.toString()
                        addShortAsuns.text = shortDesc
                        elems[0]["shortDescription"] = shortDesc!!
                        edit = true
                    }

                }
            ).show()
        }

        addLongAsuns.setOnClickListener {
            EditTextDialog(
                this,
                addLongAsuns,
                "Add Long Description", "Please, add long description",
                longDesc,
                true,
                object : OnSubmitDialog {
                    override fun setOnClickPositive(
                        view: EditText,
                        dialog: DialogInterface,
                        which: Int
                    ) {
                        longDesc = view.text.toString()
                        addLongAsuns.text = longDesc
                        elems[0]["description"] = longDesc!!
                        edit = true
                    }

                }
            ).show()
        }

        getCountAsuns()

        addImage.setOnClickListener {
            edit = true

            addedImage = findViewById(R.id.addedImage)
            frameButtonImageAdd = findViewById(R.id.frameButtonImageAdd)
            addYogaIconGrand = findViewById(R.id.addYogaIconGrand)

            getImage()
        }

        addAsuns.setOnClickListener {
            totalAll = getMapOfData()
            Log.d("map", totalAll.toString())
            Log.d("titles", titles.toString())
            if (totalAll.isEmpty()) {
                Toast.makeText(
                    this, getString(R.string.put_all_fields),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.additional_asana_title))
                    .setMessage(getString(R.string.confirm_adational_asana))
                    .setPositiveButton(getString(R.string.ending)) { _, _ ->
                        addAsunaInFire()
                    }
                    .setNegativeButton(getString(R.string.no)) { _, _ ->
                    }
                    .show()
            }
        }

        addNewAsuna.setOnClickListener {
            imagesArray.add("")
            elems.add(hashMapOf())

            images.add(mutableListOf())
            curAsana = elems.size - 1
            Log.d("CUR", curAsana.toString())

            addNew()
        }

    }

    /**
     * Обработка нажатий элементов меню
     *
     * @param item  ID нажатанного элемента
     * @return  Логическая переменная, обработать или нет
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Метод нажатия кнопки "Назад"
     *
     */
    override fun onBackPressed() {

        if (edit) {
            AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.end_adidition))
                .setMessage(getString(R.string.confirm_end_addition))
                .setPositiveButton(getString(R.string.ending)) { _, _ ->
                    for (indexing in 0 until images.size) {
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

    /**
     * Метод добавления открывающей асаны
     *
     */
    @SuppressLint("InflateParams")
    private fun addNew() {

        getDialogOld(null,"Добавить новую") { dialog, _ ->
            dialog.dismiss()

            getDialogNew(null)
        }

    }

    /**
     * Метод получения количества существующих асан из БД
     *
     */
    private fun getCountAsuns() {
        db.collection("asunaRU")
            .get()
            .addOnSuccessListener { result ->
                count = result.size() + 1
                titles.add("asuna${count}")
                elems.add(hashMapOf())
            }
    }

    /**
     * Метод открытия активити выбора локальных фото с результатом
     *
     */
    private fun getImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(Intent.createChooser(intent, "Выберете изображение"), RESULT_IMAGE)
    }

    /**
     * Метод обработки результата, возвращенного активити
     *
     * @param requestCode   Код запроса
     * @param resultCode    Код результата
     * @param data          Возвращенные данные
     */
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
                    Log.d("CUR", curAsana.toString())
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

                                val adapter = SliderAdapter(images[curAsana])
                                adapter.setOnClickOpenListener(object :
                                    OnRecyclerItemClickListener {
                                    override fun onItemClicked(position: Int, asuna: String) {
                                        curAsana = 0
                                        getImage()
                                    }

                                    override fun onItemLongClicked(position: Int) {}
                                })

                                addedImage.adapter = adapter
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

                                        val adapter = SliderAdapter(images[curAsana])
                                        adapter.setOnClickOpenListener(object :
                                            OnRecyclerItemClickListener {
                                            override fun onItemClicked(
                                                position: Int,
                                                asuna: String
                                            ) {
                                                getImage()
                                            }

                                            override fun onItemLongClicked(position: Int) {}
                                        })

                                        addedImage.adapter = adapter
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

    /**
     * Метод получения рандомной 28-ми символьной цифробуквенной строки
     *
     * @return Рандомная цифробуквенная строка
     */
    private fun getRandomString() : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..28)
            .map { allowedChars.random() }
            .joinToString("")
    }

    /**
     * Метод проверки и возврата данных. В методе происходит объединение нескольких листов
     * в один словарь. Если данных не хватает, то возвращает пустой словарь, иначе словарь данных
     *
     * @return Словарь данных добавленных асан
     */
    private fun getMapOfData() : HashMap<String, HashMap<String, Any>> {
        Log.d("ADD", elems.toString())
        Log.d("ADD", imagesArray.toString())

        var isFilledOut = true

        val hashMap: HashMap<String, HashMap<String, Any>> = hashMapOf()
        val iterator = titles.iterator()
        for ((index, title) in iterator.withIndex()) {
            if (!elems[index].containsKey("type") && elems[index]["type"] != "old") {
                val titleAsana = elems[index]["title"].toString()

                var shortDescription = ""
                if (index == 0) {
                    shortDescription = elems[index]["shortDescription"].toString()
                }
                val description = elems[index]["description"].toString()

                if (titleAsana == "null" || (shortDescription == "null" && index == 0) || description == "null" || imagesArray[index] == "") {
                    isFilledOut = false

                    if (index == 0) {
                        if (titleAsana == "null") {
                            addTitle.setTextColor(Color.RED)
                        }
                        if (shortDescription == "null") {
                            addShortAsuns.setTextColor(Color.RED)
                        }
                        if (description == "null") {
                            addLongAsuns.setTextColor(Color.RED)
                        }
                        if (imagesArray[index] == "") {
                            addImage.setBackgroundColor(Color.parseColor("#55FF0000"))
                        }
                    } else {
                        openAsansOpen[index - 1].setBackgroundColor(Color.parseColor("#55FF0000"))
                    }
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
        }

        if (!isFilledOut)
            hashMap.clear()
        return hashMap
    }

    /**
     * Метод добавления данных в БД
     *
     */
    private fun addAsunaInFire() {
        for ((title, asuna) in totalAll) {
            val ref: CollectionReference = if (title.indexOf("open") != -1) {
                db.collection("openAsunaRU")
            } else {
                db.collection("asunaRU")
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

    /**
     * Метод добавления существующей в БД открывающей асаны
     *
     * @param titleCur      Заголовок асаны
     * @param opensElems    Словарь данных асан из БД
     * @param elem          [View] изображения открывающей асаны в карточке редактирования
     * @see View
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun addOpen(titleCur: String, opensElems: HashMap<String, HashMap<String, String>>, elem: View? = null) {
        val cur = opensElems[titleCur]!!

        val imageCur = cur["thumbPath"]?.split(" ")?.get(0)
        Log.d("image", imageCur.toString())
        val idA = cur["id"].toString()

        if (elem == null) {

            titles.add(idA)
            elems[curAsana]["type"] = "old"

            getPreview(imageCur, idA, "old")

            Log.d("testing", elems.toString())
            Log.d("testing", imageCur.toString())
        } else {
            titles[curAsana] = idA
            elem.tag = "$curAsana $idA old"

            Log.d("test", "$titles $elem")

            thumbnails.child("${imageCur}.jpeg")
                .downloadUrl
                .addOnSuccessListener {
                    Glide.with(this)
                        .load(it)
                        .into(elem.findViewById(R.id.openAsanaImage))
                }
                .addOnFailureListener { exception ->
                    Log.w("gets", "Error getting documents.", exception)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        elem.findViewById<ImageView>(R.id.openAsanaImage)
                            .setImageDrawable(getDrawable(R.drawable.ic_baseline_report_problem_24))
                    }
                }
        }

        curAsana = 0
    }

    /**
     * Метод создания диалога для редактирования или добавления существующей в БД асаны
     *
     * @param elem          [View] элемент открывающей асаны в карточке редактирования асаны
     *                      (для редактирования) или null
     * @param titleDialog   Заголовок дополнительной кнопки диалога
     * @param default       ID асаны по умолчанию (для редактирования) или null
     * @param newListener   Объект слушателя нажатия на дополнительную кнопку
     * @see View
     */
    private fun getDialogOld(elem: View?, titleDialog: String, default: String? = null, newListener: DialogInterface.OnClickListener) {

        db.collection("openAsunaRU")
            .get()
            .addOnSuccessListener { result ->
                val opensTitles: MutableList<String> = mutableListOf()
                val opensElems: HashMap<String, HashMap<String, String>> = hashMapOf()
                val iter = result.iterator()
                for ((index, document) in iter.withIndex()) {
                    val title = document.data["title"].toString()
                    if (!titles.contains(document.id) || document.id == default) {
                        opensTitles.add(title)
                        opensElems[title] = hashMapOf(
                            "id" to document.id,
                            "thumbPath" to document.data["thumbPath"].toString()
                        )
                    }
                }
                val view = Spinner(this)
                val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, opensTitles)
                view.adapter = adapter
                if (default != null) {
                    view.setSelection(opensTitles.indexOf(default))
                }

                openDialog(
                    view,
                    "Add Open Asana", "Please, choose open asana",
                    { _, _ ->
                        if (default == null || view.selectedItem.toString() != default)
                            addOpen(view.selectedItem.toString(), opensElems, elem)
                    }, { _, _ ->
                        if (default == null) {
                            images[curAsana].clear()
                            imagesArray[curAsana] = ""
                        }
                        curAsana = 0
                    }, {
                        if (default == null || view.selectedItem.toString() != default)
                            addOpen(view.selectedItem.toString(), opensElems, elem)
                    },
                    titleDialog,
                    newListener
                )
            }
            .addOnCanceledListener {
                Toast.makeText(
                    this, "Ошибка загрузки",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    /**
     * Метод создания диалога для редактирования или добавления новой асаны
     *
     * @param elem          [View] элемент открывающей асаны в карточке редактирования асаны
     *                      (для редактирования) или null
     * @param titleDialog   Заголовок дополнительной кнопки диалога
     * @param newListener   Объект слушателя нажатия на дополнительную кнопку
     * @see View
     */
    @SuppressLint("CutPasteId", "UseCompatLoadingForDrawables")
    private fun getDialogNew(elem: View?, titleDialog: String? = null, newListener: DialogInterface.OnClickListener? = null) {
        val newView = View.inflate(this, R.layout.fragment_add_open, null)

        var newEditOpen: EditText

        var titleAsunaOpen: String? = null
        val titleTextOpen: TextView = newView.findViewById(R.id.addAsanaTitleOpen)

        if (elems[curAsana].containsKey("title") && elems[curAsana]["title"] != "null") {
            titleAsunaOpen = elems[curAsana]["title"]
            titleTextOpen.text = titleAsunaOpen
        } else {
            if (elem != null)
                titleTextOpen.setTextColor(Color.RED)
        }

        titleTextOpen.setOnClickListener {
            EditTextDialog(
                this,
                titleTextOpen,
                "Add Title", "Please, add title",
                titleAsunaOpen,
                false,
                object : OnSubmitDialog {
                    override fun setOnClickPositive(
                        view: EditText,
                        dialog: DialogInterface,
                        which: Int
                    ) {
                        titleAsunaOpen = view.text.toString()
                        titleTextOpen.text = titleAsunaOpen

                        edit = true
                    }

                }
            ).show()
        }

        var longDescriptionOpen: String? = null
        val longTextOpen: TextView = newView.findViewById(R.id.addLongDescriptionOpen)

        if (elems[curAsana].containsKey("description") && elems[curAsana]["description"] != "null") {
            longDescriptionOpen = elems[curAsana]["description"]
            longTextOpen.text = longDescriptionOpen
        } else {
            if (elem != null)
                longTextOpen.setTextColor(Color.RED)
        }

        longTextOpen.setOnClickListener {
            EditTextDialog(
                this,
                longTextOpen,
                "Add Title", "Please, add title",
                longDescriptionOpen,
                true,
                object : OnSubmitDialog {
                    override fun setOnClickPositive(
                        view: EditText,
                        dialog: DialogInterface,
                        which: Int
                    ) {
                        longDescriptionOpen = view.text.toString()
                        longTextOpen.text = longDescriptionOpen

                        edit = true
                    }

                }
            ).show()
        }

        val addPhotoOpen: ImageButton = newView.findViewById(R.id.addPhotoOpen)
        addedImage = newView.findViewById(R.id.addedImageOpen)
        frameButtonImageAdd = newView.findViewById(R.id.frameButtonImageAddOpen)
        addYogaIconGrand = newView.findViewById(R.id.addYogaIconGrandOpen)

        if (images[curAsana].isNotEmpty()) {

            thumbnails.child("${images[curAsana][0]}.jpeg")
                .downloadUrl
                .addOnSuccessListener {
                    Glide.with(this)
                        .load(it)
                        .into(addYogaIconGrand)
                }
                .addOnFailureListener { exception ->
                    Log.w("gets", "Error getting documents.", exception)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        addYogaIconGrand
                            .setImageDrawable(getDrawable(R.drawable.ic_baseline_report_problem_24))
                    }
                }

            val adapter = SliderAdapter(images[curAsana])
            adapter.setOnClickOpenListener(object :
                OnRecyclerItemClickListener {
                override fun onItemClicked(
                    position: Int,
                    asuna: String
                ) {
                    getImage()
                }

                override fun onItemLongClicked(position: Int) {}
            })

            addedImage.adapter = adapter
            frameButtonImageAdd.visibility = View.GONE
            addedImage.visibility = View.VISIBLE

        } else {
            if (elem != null)
                addPhotoOpen.setBackgroundColor(Color.parseColor("#55FF0000"))
        }

        addPhotoOpen.setOnClickListener {
            getImage()
        }

        openDialog(
            newView,
            "Add Open Asana", "Please, put all values in field",
            { _, _ ->
                if (elem == null) {
                    countOpenNew++
                    val titleOpenNew = "open_asana${count}_${countOpenNew}"
                    titles.add(titleOpenNew)

                    elems[curAsana]["title"] = titleAsunaOpen.toString()
                    elems[curAsana]["description"] = longDescriptionOpen.toString()

                    var imageCur: String? = null
                    if (images[curAsana].isNotEmpty()) {
                        imageCur = images[curAsana][0]
                    }
                    var isNull = false
                    if (titleAsunaOpen.isNullOrEmpty() || longDescriptionOpen.isNullOrEmpty() || images[curAsana].isEmpty()) {
                        isNull = true
                    }
                    getPreview(imageCur, titleOpenNew, "new", isNull)
                } else {
                    elems[curAsana]["title"] = titleAsunaOpen.toString()
                    elems[curAsana]["description"] = longDescriptionOpen.toString()

                    var imageCur: String? = null
                    if (images[curAsana].isNotEmpty()) {
                        imageCur = images[curAsana][0]
                    }

                    thumbnails.child("${imageCur}.jpeg")
                        .downloadUrl
                        .addOnSuccessListener {
                            Glide.with(this)
                                .load(it)
                                .into(elem.findViewById(R.id.openAsanaImage))
                        }
                        .addOnFailureListener { exception ->
                            Log.w("gets", "Error getting documents.", exception)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                elem.findViewById<ImageView>(R.id.openAsanaImage)
                                    .setImageDrawable(getDrawable(R.drawable.ic_baseline_report_problem_24))
                            }
                        }

                    if (titleAsunaOpen.isNullOrEmpty() || longDescriptionOpen.isNullOrEmpty() || images[curAsana].isEmpty()) {
                        elem.setBackgroundColor(Color.parseColor("#55FF0000"))
                    } else {
                        elem.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
                    }
                }
                curAsana = 0
            }, { _, _ ->
                if (elem == null) {
                    for (item in images[curAsana]) {
                        avatars.child("thumbnails/$item.jpeg").delete()
                    }
                    images[curAsana].clear()
                    imagesArray[curAsana] = ""
                }
                curAsana = 0
            }, {
                if (elem == null) {
                    countOpenNew++
                    val titleOpenNew = "open_asana${count}_${countOpenNew}"
                    titles.add(titleOpenNew)

                    elems[curAsana]["title"] = titleAsunaOpen.toString()
                    elems[curAsana]["description"] = longDescriptionOpen.toString()

                    var imageCur: String? = null
                    if (images[curAsana].isNotEmpty()) {
                        imageCur = images[curAsana][0]
                    }

                    var isNull = false
                    if (titleAsunaOpen.isNullOrEmpty() || longDescriptionOpen.isNullOrEmpty() || images[curAsana].isEmpty()) {
                        isNull = true
                    }

                    getPreview(imageCur, titleOpenNew, "new", isNull)
                } else {
                    elems[curAsana]["title"] = titleAsunaOpen.toString()
                    elems[curAsana]["description"] = longDescriptionOpen.toString()

                    var imageCur: String? = null
                    if (images[curAsana].isNotEmpty()) {
                        imageCur = images[curAsana][0]
                    }

                    thumbnails.child("${imageCur}.jpeg")
                        .downloadUrl
                        .addOnSuccessListener {
                            Glide.with(this)
                                .load(it)
                                .into(elem.findViewById(R.id.openAsanaImage))
                        }
                        .addOnFailureListener { exception ->
                            Log.w("gets", "Error getting documents.", exception)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                elem.findViewById<ImageView>(R.id.openAsanaImage)
                                    .setImageDrawable(getDrawable(R.drawable.ic_baseline_report_problem_24))
                            }
                        }

                    if (titleAsunaOpen.isNullOrEmpty() || longDescriptionOpen.isNullOrEmpty() || images[curAsana].isEmpty()) {
                        elem.setBackgroundColor(Color.parseColor("#55FF0000"))
                    } else {
                        elem.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
                    }
                }
                curAsana = 0
            },
            titleDialog,
            newListener
        )
    }

    /**
     * Метод создания превью открывающей асаны
     *
     * @param imageCur  Ссылка на фото в виде строки
     * @param idA       ID добавленной асаны
     * @param curType   Тип добавленной асаны (новая или старая)
     * @param isNull    Все ли данные асаны добавлены. Если нет, то добавится красная рамка ошибки
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getPreview(imageCur: String?, idA: String, curType: String, isNull: Boolean = false) {
        val inflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView: View = inflater.inflate(R.layout.open_photo, null)

        if (isNull) {
            rowView.setBackgroundColor(Color.parseColor("#55FF0000"))
        }

        openAsansOpen.addView(rowView, openAsansOpen.childCount - 1)

        thumbnails.child("${imageCur}.jpeg")
            .downloadUrl
            .addOnSuccessListener {
                Glide.with(this)
                    .load(it)
                    .into(rowView.findViewById(R.id.openAsanaImage))
            }
            .addOnFailureListener { exception ->
                Log.w("gets", "Error getting documents.", exception)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    rowView.findViewById<ImageView>(R.id.openAsanaImage)
                        .setImageDrawable(getDrawable(R.drawable.ic_baseline_report_problem_24))
                }
            }

        rowView.tag = "$curAsana $idA $curType"
        rowView.setOnClickListener {
            val listTag = it.tag.toString().split(" ")

            val id = listTag[0].toInt()
            val name = listTag[1]
            val type = listTag[2]

            curAsana = id

            if (type == "old") {
                getDialogOld(
                    it,
                    "Удалить",
                    name
                ) { _, _ ->
                    images.removeAt(curAsana)
                    imagesArray.removeAt(curAsana)
                    titles.remove(name)
                    openAsansOpen.removeView(it)
                    elems.removeAt(curAsana)
                    curAsana = 0
                }
            }
            if (type == "new") {
                getDialogNew(
                    it,
                    "Удалить",
                ) { _, _ ->
                    for (item in images[curAsana]) {
                        avatars.child("thumbnails/$item.jpeg").delete()
                    }
                    images.removeAt(curAsana)
                    imagesArray.removeAt(curAsana)
                    countOpenNew--
                    titles.remove(name)
                    elems.removeAt(curAsana)
                    openAsansOpen.removeView(it)
                    curAsana = 0
                }
            }
        }
    }

    /**
     * Метод создания диалога
     *
     * @param view              [View] элемент, добавляемый в диалог
     * @param title             Заголовок диалога
     * @param message           Сообщение диалога
     * @param listener          Слушатель нажатия на положитильную кнопку
     * @param listenerNegative  Слушатель нажатия на отрицательную кнопку
     * @param onCancel          Событие закрытия окна
     * @param titleAdd          Название дополнительной кнопки (опционально)
     * @param newListener       Слушатель дополнительной кнопки (опционально)
     * @see View
     */
    private fun openDialog(
        view: View,
        title: String,
        message: String,
        listener: DialogInterface.OnClickListener,
        listenerNegative: DialogInterface.OnClickListener,
        onCancel: DialogInterface.OnCancelListener,
        titleAdd: String? = null,
        newListener: DialogInterface.OnClickListener? = null
    ) {
        val alert = AlertDialog.Builder(this)

        alert.setTitle(title)
        alert.setMessage(message)

        alert.setView(view)

        alert.setPositiveButton("Ok", listener)
        if (titleAdd != null)
            alert.setNeutralButton(titleAdd, newListener)

        alert.setNegativeButton("Cancel", listenerNegative)

        alert.setOnCancelListener(onCancel)

        alert.show()
    }

}