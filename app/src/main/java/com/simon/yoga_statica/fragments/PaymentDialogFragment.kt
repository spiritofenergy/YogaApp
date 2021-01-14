package com.simon.yoga_statica.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.simon.yoga_statica.R
//import ru.yoo.sdk.kassa.payments.*
import java.math.BigDecimal
import java.util.*

class PaymentDialogFragment : DialogFragment() {

    private val REQUEST_CODE_TOKENIZE = 1234

    private lateinit var saleTxt: TextView
    private lateinit var priceTxt: TextView
    private lateinit var payBtn: Button

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

        payBtn = rootView.findViewById(R.id.pay_btn)
        payBtn.setOnClickListener {
            timeToStartCheckout()
        }

        return rootView
    }

    override fun onStart() {
        super.onStart()

        if (sale != 0) {
            saleTxt.visibility = View.VISIBLE
            priceTxt.text = "${price - (price * sale / 100)}.00 ₽"
        }
    }

    fun timeToStartCheckout() {
//        val paymentParameters = PaymentParameters(
//                Amount(BigDecimal.valueOf(price - (price * sale / 100).toDouble()), Currency.getInstance("RUB")),
//                getString(R.string.title_item),
//                "8 онлайн занятий йоги с инструктором",
//                "live_AAAAAAAAAAAAAAAAAAAA",
//                "778376",
//                SavePaymentMethod.OFF
//        )
//
//        val testParameters = TestParameters(
//            true,
//            true,
//            MockConfiguration(
//                false,
//                true,
//                5,
//                Amount(BigDecimal.valueOf(price - (price * sale / 100).toDouble()), Currency.getInstance("RUB"))))
//
//        val intent: Intent = Checkout.createTokenizeIntent(requireContext(), paymentParameters, testParameters)
//        startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }

}