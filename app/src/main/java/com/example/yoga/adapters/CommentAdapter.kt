package com.example.yoga.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yoga.classes.Comment
import com.example.yoga.views.CommentItemView

class CommentAdapter(private val list: List<Comment>) : RecyclerView.Adapter<CommentItemView>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentItemView {
        val inflater = LayoutInflater.from(parent.context)
        return CommentItemView(inflater, parent)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: CommentItemView, position: Int) {
        val comment: Comment = list[position]
        holder.bind(comment)
    }
}