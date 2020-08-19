package com.example.yoga.activies

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yoga.R
import com.example.yoga.adapters.CardAdapter
import com.example.yoga.adapters.CommentAdapter
import com.example.yoga.classes.Card
import com.example.yoga.classes.Comment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.time.LocalDateTime
import java.util.*

class AsunaActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val thumbnails: StorageReference = storage.reference.child("thumbnails")

    private lateinit var title: TextView
    private lateinit var textDescription: TextView
    private lateinit var imageWorkout: ImageView
    private lateinit var commentPersonName: EditText
    private lateinit var comment: EditText
    private lateinit var commentSend: Button
    private lateinit var commentList: RecyclerView

    private var commentArr = mutableListOf<Comment>()

    private var countComment = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asuna)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = findViewById(R.id.textTitle)
        textDescription = findViewById(R.id.textDescription)
        imageWorkout = findViewById(R.id.imageWorkout)
        commentSend = findViewById(R.id.commentSend)
        commentPersonName = findViewById(R.id.commentPersonName)
        comment = findViewById(R.id.comment)
        commentList = findViewById(R.id.commentList)


        val intent = intent
        val id = intent.getStringExtra("asunaID")

        db.collection("asunaRU").document(id.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    title.text = document.data?.get("title").toString()
                    textDescription.text = document.data?.get("description").toString()
                    countComment = (document.data?.get("comments") as Long).toInt()
                    thumbnails.child("${document.data?.get("thumbPath")}.jpeg")
                        .downloadUrl
                        .addOnSuccessListener {
                            Glide.with(this)
                                .load(it)
                                .into(imageWorkout)
                        }.addOnFailureListener { exception ->
                            Log.d("log", "get failed with ", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("gets", "Error getting documents.", exception)
            }

        db.collection("comments")
            .whereEqualTo("id", id.toString())
            .get()
            .addOnSuccessListener { documents ->
                Log.d("count", documents.size().toString())
                for (document in documents) {
                    val comment = Comment()
                    comment.name = document.data["name"].toString()
                    comment.comment = document.data["comment"].toString()

                    commentArr.add(comment)
                }

                commentList.apply {
                    layoutManager = LinearLayoutManager(this.context)
                    adapter = CommentAdapter(commentArr)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("gets", "Error getting documents.", exception)
            }

        commentSend.setOnClickListener {
            if(commentPersonName.text.isNotEmpty() && comment.text.isNotEmpty()) {
                val commentMap = hashMapOf(
                    "id" to id.toString(),
                    "name" to commentPersonName.text.toString(),
                    "comment" to comment.text.toString()
                )

                commentPersonName.setText("")
                comment.setText("")


                db.collection("comments")
                    .add(commentMap)
                countComment += 1
                db.collection("asunaRU").document(id.toString())
                    .update("comments", countComment)
            } else {
                Toast.makeText(baseContext, "Заполните пустые поля",
                    Toast.LENGTH_SHORT).show()
            }
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
}

//"time" to now()