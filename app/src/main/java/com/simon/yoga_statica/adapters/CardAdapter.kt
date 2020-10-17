package com.simon.yoga_statica.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.simon.yoga_statica.classes.Ad
import com.simon.yoga_statica.classes.Card
import com.simon.yoga_statica.interfaces.OnRecyclerItemClickListener
import com.simon.yoga_statica.views.AdItemView
import com.simon.yoga_statica.views.CardItemView

class CardAdapter(private val list: List<Any>,private val fragmentManager: FragmentManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var addListener: OnRecyclerItemClickListener? = null
    private var openListener: OnRecyclerItemClickListener? = null
    var cardCount = 0
    var indexAdv = 0
    var countAdv = 0

    private val TYPE_CARD = 1
    private val TYPE_AD = 0

    fun setOnClickAdd(listener: OnRecyclerItemClickListener) {
        addListener = listener
    }

    fun setOnClickOpen(listener: OnRecyclerItemClickListener) {
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
            holder.bind()

            return
        }
        holder as CardItemView
        val card: Card = list[position] as Card
        card.allCardCount = cardCount
        if (indexAdv != 0)
            card.currentCardNum = position + 1 - (position / (indexAdv + 1))
        else
            card.currentCardNum = position + 1
        holder.bind(card, fragmentManager, openListener)

        holder.addAsuna.setOnClickListener {
            addListener?.onItemClicked(card.id, position)
        }

        holder.titleCard.setOnClickListener {
            openListener?.onItemClicked(card.id, position)
        }

        holder.imgFrame.setOnClickListener {
            openListener?.onItemClicked(card.id, position)
        }

        holder.social.setOnClickListener {
            openListener?.onItemClicked(card.id, position)
        }

    }
    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return if (list[position] is Ad) TYPE_AD else TYPE_CARD
    }
}
