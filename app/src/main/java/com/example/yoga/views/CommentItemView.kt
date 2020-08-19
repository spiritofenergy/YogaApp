package com.example.yoga.views

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yoga.R
import com.example.yoga.classes.Comment

class CommentItemView(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.comment_item, parent, false)) {
    private var commentName: TextView = itemView.findViewById(R.id.nameCommentCard)
    private var commentText: TextView = itemView.findViewById(R.id.textCommentCard)

    fun bind(comment: Comment) {
        commentName.text = comment.name
        commentText.text = comment.comment
    }
}