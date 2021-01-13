package com.simon.yoga_statica.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.simon.yoga_statica.R

class PaymentDialogFragment : DialogFragment() {

    private lateinit var saleTxt: TextView
    private lateinit var priceTxt: TextView

    var sale: Int = 0
    private val price: Int = 1600

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View = inflater.inflate(R.layout.fragment_payment, container, false)

        saleTxt = rootView.findViewById(R.id.sale_price)
        priceTxt = rootView.findViewById(R.id.price_txt)

        return rootView
    }

    override fun onStart() {
        super.onStart()

        if (sale != 0) {
            saleTxt.visibility = View.VISIBLE
            priceTxt.text = "${price - (price * sale / 100)}.00 ₽"
        }
    }

}