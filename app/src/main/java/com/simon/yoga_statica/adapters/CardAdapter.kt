package com.simon.yoga_statica.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.simon.yoga_statica.classes.Card
import com.simon.yoga_statica.interfaces.OnClickOpenListener
import com.simon.yoga_statica.interfaces.OnRecyclerItemClickListener
import com.simon.yoga_statica.views.AdItemView
import com.simon.yoga_statica.views.CardItemView
import java.util.*

class CardAdapter(private val list: List<Any>,private val fragmentManager: FragmentManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var listener: OnRecyclerItemClickListener? = null
    private var openListener: OnClickOpenListener? = null
    var cardCount = 0
    var indexAdv = 0

    private val TYPE_CARD = 1
    private val TYPE_AD = 0

    fun setOnClickItemAddListener(listener: OnRecyclerItemClickListener) {
        this.listener = listener
    }

    fun setOnClickOpen(listener: OnClickOpenListener) {
        openListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == TYPE_AD) {
            return AdItemView(inflater, parent)
        }
        return CardItemView(inflater, parent)
     }



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)

        if (viewType == TYPE_AD) {
            holder as AdItemView
            holder.bind(list[position] as UnifiedNativeAd)

            return
        }
        holder as CardItemView
        val card: Card = list[position] as Card
        card.allCardCount = cardCount
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

    override fun getItemViewType(position: Int): Int {
        return if (list[position] is UnifiedNativeAd) TYPE_AD else TYPE_CARD
    }
}
