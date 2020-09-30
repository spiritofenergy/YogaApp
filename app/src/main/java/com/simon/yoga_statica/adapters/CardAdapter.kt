package com.simon.yoga_statica.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.simon.yoga_statica.classes.Card
import com.simon.yoga_statica.interfaces.OnClickOpenListener
import com.simon.yoga_statica.interfaces.OnRecyclerItemClickListener
import com.simon.yoga_statica.views.CardItemView

class CardAdapter(private val list: List<Card>,private val fragmentManager: FragmentManager) : RecyclerView.Adapter<CardItemView>() {
    private var listener: OnRecyclerItemClickListener? = null
    private var openListener: OnClickOpenListener? = null
    fun setOnClickItemAddListener(listener: OnRecyclerItemClickListener) {
        this.listener = listener
    }

    fun setOnClickOpen(listener: OnClickOpenListener) {
        openListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardItemView {
        val inflater = LayoutInflater.from(parent.context)
        return CardItemView(inflater, parent)
     }



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: CardItemView, position: Int) {
        val card: Card = list[position]
        card.allCardCount = itemCount
        card.currentCardNum = position + 1
        holder.bind(card, fragmentManager)

        holder.addAsuna.setOnClickListener {
            listener?.onItemClicked(card.id, position)
        }

        holder.titleCard.setOnClickListener {
            openListener?.onClick(card.id, position)
        }

        holder.image.setOnClickListener {
            openListener?.onClick(card.id, position)
        }

        holder.social.setOnClickListener {
            openListener?.onClick(card.id, position)
        }
    }
    override fun getItemCount(): Int = list.size
}
