package com.example.yoga.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yoga.classes.Card
import com.example.yoga.views.CardItemView

class CardAdapter(private val list: List<Card>) : RecyclerView.Adapter<CardItemView>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardItemView {
        val inflater = LayoutInflater.from(parent.context)
        return CardItemView(inflater, parent)
    }
    override fun onBindViewHolder(holder: CardItemView, position: Int) {
        val card: Card = list[position]
        card.allCardCount = itemCount
        card.currentCardNum = position + 1
        holder.bind(card)
    }
    override fun getItemCount(): Int = list.size

}