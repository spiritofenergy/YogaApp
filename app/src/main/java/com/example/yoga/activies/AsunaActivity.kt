package com.example.yoga.activies

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yoga.R
import com.example.yoga.adapters.CommentAdapter
import com.example.yoga.classes.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.type.DateTime
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter

class AsunaActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private lateinit var auth: FirebaseAuth
    private val thumbnails: StorageReference = storage.reference.child("thumbnails")

    private lateinit var title: TextView
    private lateinit var textDescription: TextView
    private lateinit var imageWorkout: ImageView
    private lateinit var comment: EditText
    private lateinit var commentSend: Button
    private lateinit var commentList: RecyclerView
    private lateinit var commentAddArea: LinearLayout

    lateinit var name: String

    private var commentArr = mutableListOf<Comment>()

    private var countComment = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asuna)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val actionBar = supportActionBar

        auth = Firebase.auth

        title = findViewById(R.id.textTitle)
        textDescription = findViewById(R.id.textDescription)
        imageWorkout = findViewById(R.id.imageWorkout)
        commentSend = findViewById(R.id.commentSend)
        comment = findViewById(R.id.comment)
        commentList = findViewById(R.id.commentList)
        commentAddArea = findViewById(R.id.commentAddArea)


        val receivedIntent = intent
        val id = receivedIntent.getStringExtra("asunaID")

        db.collection("asunaRU").document(id.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    actionBar!!.title = document.data?.get("title").toString()
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

        db.collection("users")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        name = document["name"].toString()
                    }
                }

                if (::name.isInitialized) {
                    name = "User"
                }
            }
            .addOnFailureListener { exception ->
                Log.w("home", "Error getting documents: ", exception)
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

                    val list = document.data["time"] as Map<*, *>
                    comment.datetime["day"] = (list["dayOfMonth"] as Long).toInt()
                    comment.datetime["month"] = (list["monthValue"] as Long).toInt()
                    comment.datetime["year"] = (list["year"] as Long).toInt()
                    comment.datetime["hour"] = (list["hour"] as Long).toInt()
                    comment.datetime["minute"] = (list["minute"] as Long).toInt()

                    comment.time = "${comment.datetime["day"]}.${comment.datetime["month"]}.${comment.datetime["year"]} ${comment.datetime["hour"]}:${comment.datetime["minute"]}"

                    commentArr.add(comment)
                }

                commentArr.sortWith(
                    compareBy<Comment> { it.datetime["year"] }
                        .thenBy { it.datetime["month"] }
                        .thenBy { it.datetime["day"] }
                        .thenBy { it.datetime["hour"] }
                        .thenBy { it.datetime["minute"] }
                )

                commentArr.reverse()

                commentList.apply {
                    layoutManager = LinearLayoutManager(this.context)
                    adapter = CommentAdapter(commentArr)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("gets", "Error getting documents.", exception)
            }

        commentSend.setOnClickListener {
            if(comment.text.isNotEmpty()) {
                val commentMap = hashMapOf(
                    "id" to id.toString(),
                    "name" to name,
                    "comment" to comment.text.toString(),
                    "time" to now()
                )

                comment.setText("")


                db.collection("comments")
                    .add(commentMap)
                    .addOnCompleteListener {
                        countComment += 1
                        db.collection("asunaRU").document(id.toString())
                            .update("comments", countComment)

                        val commentArrCur = mutableListOf<Comment>()

                        val comment = Comment()
                        comment.name = commentMap["name"].toString()
                        comment.comment = commentMap["comment"].toString()

                        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                        val formatted = (commentMap["time"] as LocalDateTime).format(formatter)

                        comment.time = formatted

                        commentArrCur.add(comment)
                        commentArrCur.addAll(commentArr)

                        commentList.apply {
                            layoutManager = LinearLayoutManager(this@AsunaActivity)
                            adapter = CommentAdapter(commentArrCur)
                        }
                    }

            } else {
                Toast.makeText(baseContext, "Заполните пустые поля",
                    Toast.LENGTH_SHORT).show()
            }
        }

        if (auth.currentUser == null) {
            commentAddArea.visibility = View.GONE
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