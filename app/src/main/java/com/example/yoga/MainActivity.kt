package com.example.yoga

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yoga.adapters.CardAdapter
import com.example.yoga.classes.Card

class MainActivity : AppCompatActivity() {

    private lateinit var cardsRecyclerView: RecyclerView

    private val cardsArr = listOf(
        Card(),
        Card(),
        Card(),
        Card(),
        Card(),
        Card(),
        Card()
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cardsRecyclerView = findViewById(R.id.cards)

        cardsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = CardAdapter(cardsArr)
        }
    }
}