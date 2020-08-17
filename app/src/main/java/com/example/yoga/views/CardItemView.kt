package com.example.yoga.views

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yoga.R
import com.example.yoga.classes.Card

class CardItemView(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_view, parent, false)) {
    private var counterTwo: TextView = itemView.findViewById(R.id.counterTwo)
    private var counterFirst: TextView = itemView.findViewById(R.id.counterFirst)

    fun bind(card: Card) {
        counterTwo.text = card.currentCardNum.toString()
        counterFirst.text = card.allCardCount.toString()
    }
}