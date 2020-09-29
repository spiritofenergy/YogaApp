package com.simon.yoga_statica.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.simon.yoga_statica.R
import com.simon.yoga_statica.adapters.CommentAdapter
import com.simon.yoga_statica.classes.Comment
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AsunaFragment : Fragment() {

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
    private var idAsuna: String = "asuna01"


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_asuna, container, false)

        auth = Firebase.auth

        title = rootView.findViewById(R.id.textTitle)
        textDescription = rootView.findViewById(R.id.textDescription)
        imageWorkout = rootView.findViewById(R.id.imageWorkout)
        commentSend = rootView.findViewById(R.id.commentSend)
        comment = rootView.findViewById(R.id.comment)
        commentList = rootView.findViewById(R.id.commentList)
        commentAddArea = rootView.findViewById(R.id.commentAddArea)

        db.collection("asunaRU").document(idAsuna)
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
            .whereEqualTo("id", idAsuna)
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
                    "id" to idAsuna,
                    "name" to name,
                    "comment" to comment.text.toString(),
                    "time" to LocalDateTime.now()
                )

                comment.setText("")


                db.collection("comments")
                    .add(commentMap)
                    .addOnCompleteListener {
                        countComment += 1
                        db.collection("asunaRU").document(idAsuna)
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
                            layoutManager = LinearLayoutManager(activity)
                            adapter = CommentAdapter(commentArrCur)
                        }
                    }

            } else {
                Toast.makeText(activity, "Заполните пустые поля",
                    Toast.LENGTH_SHORT).show()
            }
        }

        if (auth.currentUser == null) {
            commentAddArea.visibility = View.GONE
        }

        return rootView
    }

    fun setAsuna(id: String) {
        idAsuna = id
    }
}