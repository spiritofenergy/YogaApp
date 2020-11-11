package com.simon.yoga_statica.adapters

import android.content.Context
import android.os.Build

import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.simon.yoga_statica.interfaces.OnRecyclerItemClickListener
import com.simon.yoga_statica.views.SliderView

class SliderAdapter(private val images: List<String>) : RecyclerView.Adapter<SliderView>()  {

    private var clickListener: OnRecyclerItemClickListener? = null
    private var idAsuna: String? = null

    fun setOnClickOpenListener(listener: OnRecyclerItemClickListener, id: String? = null) {
        clickListener = listener
        idAsuna = id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderView {
        val inflater = LayoutInflater.from(parent.context)
        return SliderView(inflater, parent)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: SliderView, position: Int) {
        holder.bind(images[position])

        if (clickListener != null) {
            holder.image.setOnClickListener {
                if (idAsuna != null)
                    clickListener?.onItemClicked(position, idAsuna.toString())
                else
                    clickListener?.onItemClicked(position)
            }
        }
    }

    override fun getItemCount(): Int = images.size
}