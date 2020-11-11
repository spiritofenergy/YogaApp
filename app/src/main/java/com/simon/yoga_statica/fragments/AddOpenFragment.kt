package com.simon.yoga_statica.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.SearchManager
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.simon.yoga_statica.R
import com.simon.yoga_statica.adapters.SliderAdapter
import com.simon.yoga_statica.interfaces.OnRecyclerItemClickListener
import kotlinx.android.synthetic.main.fragment_add_open.view.*

class AddOpenFragment(private val listenerClose: DialogInterface.OnCancelListener) : DialogFragment() {

    private val storage = Firebase.storage
    private val avatars: StorageReference = storage.reference

    private lateinit var addAsanaTitleOpen: TextView
    private lateinit var addLongDescriptionOpen: TextView
    private lateinit var addPhotoOpen: ImageButton
    private lateinit var addYogaIconGrand: ImageView
    private lateinit var addedImage: ViewPager2
    private lateinit var frameButtonImageAdd: FrameLayout
    private lateinit var addAsuns: Button

    private var edit = false
    private var titleAsuna = ""
    private var longDesc = ""
    private lateinit var newEdit: EditText
    private var imagesStr = ""
    private var imagesArray: MutableList<String> = mutableListOf("")

    private val RESULT_IMAGE = 3214

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_add_open, container, false)

        prefs = activity?.getSharedPreferences("settings", Context.MODE_PRIVATE)!!

        val theme = prefs.getString(APP_PREFERENCES_THEME, "default")

        addAsanaTitleOpen = rootView.addAsanaTitleOpen
        addLongDescriptionOpen = rootView.addLongDescriptionOpen
        addPhotoOpen = rootView.addPhotoOpen
        addYogaIconGrand = rootView.addYogaIconGrandOpen
        addedImage = rootView.addedImageOpen
        frameButtonImageAdd = rootView.frameButtonImageAddOpen

        addAsuns.background = ContextCompat.getDrawable(
            activity!!,
            resources.getIdentifier(
                "inset_button_$theme",
                "drawable",
                activity?.packageName
            )
        )

        addAsanaTitleOpen.setOnClickListener {
            openDialog("Add Title", "Please, add title",
                { _, _ ->
                    if (newEdit.text.toString() != "") {
                        titleAsuna = newEdit.text.toString()
                        addAsanaTitleOpen.text = titleAsuna

                        edit = true

                        if (Build.VERSION.SDK_INT >= 23)
                            addAsanaTitleOpen.setTextColor(context?.getColor(R.color.colorTextTitle)!!)
                        else
                            addAsanaTitleOpen.setTextColor(resources.getColor(R.color.colorTextTitle))
                    } else
                        if (titleAsuna == "")
                            addAsanaTitleOpen.setTextColor(Color.RED)
                }, { _, _ ->
                    if (titleAsuna == "")
                        addAsanaTitleOpen.setTextColor(Color.RED)
                }, {
                    if (titleAsuna == "")
                        addAsanaTitleOpen.setTextColor(Color.RED)
                }, default = titleAsuna
            )
        }

        addLongDescriptionOpen.setOnClickListener {
            openDialog("Add Long Description", "Please, add long description",
                { _, _ ->
                    if (newEdit.text.toString() != "") {
                        longDesc = newEdit.text.toString()
                        addLongDescriptionOpen.text = longDesc
                        edit = true

                        if (Build.VERSION.SDK_INT >= 23)
                            addLongDescriptionOpen.setTextColor(context?.getColor(R.color.colorTextTitle)!!)
                        else
                            addLongDescriptionOpen.setTextColor(resources.getColor(R.color.colorTextTitle))
                    } else
                        if (longDesc == "")
                            addLongDescriptionOpen.setTextColor(Color.RED)
                }, { _, _ ->
                    if (longDesc == "")
                        addLongDescriptionOpen.setTextColor(Color.RED)
                }, {
                    if (longDesc == "")
                        addLongDescriptionOpen.setTextColor(Color.RED)
                }, true, longDesc
            )
        }

        addPhotoOpen.setOnClickListener {
            edit = true
            getImage()
        }

        return rootView
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val width: Int = ViewGroup.LayoutParams.MATCH_PARENT
            val height: Int = ViewGroup.LayoutParams.WRAP_CONTENT

            dialog?.window?.setLayout(width, height)
        }
    }



    private fun openDialog(
        title: String,
        message: String,
        listener: DialogInterface.OnClickListener,
        listenerNegative: DialogInterface.OnClickListener,
        onCancel: DialogInterface.OnCancelListener,
        multiline: Boolean = false,
        default: String? = null
    ) {
        val alert = AlertDialog.Builder(context)

        alert.setTitle(title)
        alert.setMessage(message)

        newEdit = EditText(context)
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

    private fun getImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(Intent.createChooser(intent, "Выберете изображение"), RESULT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RESULT_IMAGE -> {
                    for (item in imagesArray) {
                        avatars.child("thumbnails/$item.jpeg").delete()
                    }
                    imagesArray.clear()
                    val selectedImageUri: Uri? = data?.data

                    if (selectedImageUri != null) {
                        val nameImg = getRandomString()
                        val upload: UploadTask = avatars
                            .child("thumbnails/$nameImg.jpeg")
                            .putFile(selectedImageUri)

                        imagesArray.add(nameImg)
                        imagesStr = imagesArray.joinToString(" ")

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

                                val adapter = SliderAdapter(imagesArray)
                                adapter.setOnClickOpenListener(object :
                                    OnRecyclerItemClickListener {
                                    override fun onItemClicked(position: Int, asuna: String) {
                                        getImage()
                                    }

                                    override fun onItemLongClicked(position: Int) {}
                                })

                                addedImage.adapter = adapter
                                frameButtonImageAdd.visibility = View.GONE
                                addedImage.visibility = View.VISIBLE
                            } else {
                                Toast.makeText(
                                    context, getString(R.string.error_upload) + task.exception,
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

                                imagesArray.add(nameImg)

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

                                        if (imagesArray[0] == nameImg) {
                                            Glide.with(this)
                                                .load(downloadUri)
                                                .into(addYogaIconGrand)
                                        }

                                        val adapter = SliderAdapter(imagesArray)
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
                                            context, getString(R.string.error_upload) + task.exception,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                            imagesStr = imagesArray.joinToString(" ")
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
}