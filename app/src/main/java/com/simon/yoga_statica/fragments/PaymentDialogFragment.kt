package com.simon.yoga_statica.fragments

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.DialogFragment
import com.simon.yoga_statica.R
import ru.yoo.sdk.kassa.payments.*
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
    private var totalPrice: Int = 0

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

            totalPrice = price - (price * sale / 100)

            priceTxt.text = "${totalPrice}.00 ₽"
        }
    }

    fun timeToStartCheckout() {
        val paymentParameters = PaymentParameters(
            Amount(BigDecimal.valueOf(totalPrice.toDouble()), Currency.getInstance("RUB")),
            getString(R.string.title_item),
            "8 онлайн занятий йоги с инструктором",
            "test_Nzc4Mzc2u5N-ELPsbelP3rfoWi7uuC3kgl4I16MUZzo",
            "778376",
            SavePaymentMethod.OFF,
            setOf(PaymentMethodType.BANK_CARD, PaymentMethodType.SBERBANK)
        )

        val testParameters = TestParameters(
            true,
            true,
            MockConfiguration(
                false,
                true,
                5,
                Amount(BigDecimal.valueOf(totalPrice.toDouble() * 4 / 100), Currency.getInstance("RUB")))
        )

        val uiParameters = UiParameters(
            false,
            ColorScheme(getPrimaryColor())
        )

        val intent: Intent = Checkout.createTokenizeIntent(requireContext(), paymentParameters, testParameters, uiParameters)
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_TOKENIZE) {
            when (resultCode) {
                RESULT_OK -> {
                    this.dismiss()

                    if (data != null) {
                        val result: TokenizationResult = Checkout.createTokenizationResult(data)
                    }
                }
                RESULT_CANCELED -> { }
            }
        }
    }

    fun timeToStart3DS() {
        val intent: Intent = Checkout.create3dsIntent(
                requireContext(),
                "https://3dsurl.com/"
        );
        startActivityForResult(intent, 1);
    }

    private fun getPrimaryColor() : Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)

        return typedValue.data
    }

}