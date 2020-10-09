package com.simon.yoga_statica.adapters

import android.content.Context

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simon.yoga_statica.views.SliderView

class SliderAdapter(private val images: List<String>) : RecyclerView.Adapter<SliderView>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderView {
        val inflater = LayoutInflater.from(parent.context)
        return SliderView(inflater, parent)
    }

    override fun onBindViewHolder(holder: SliderView, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size
}