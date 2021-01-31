package com.simon.yoga_statica.fragments

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.simon.yoga_statica.R
import com.simon.yoga_statica.classes.Payment
import com.simon.yoga_statica.viewmodels.PromocodeFragmentViewModel
import ru.yoo.sdk.kassa.payments.*
import java.math.BigDecimal
import java.util.*

class PaymentDialogFragment : DialogFragment() {

    private val REQUEST_CODE_TOKENIZE = 1234

    private lateinit var saleTxt: TextView
    private lateinit var priceTxt: TextView
    private lateinit var payBtn: Button

    private lateinit var loadDialog: Dialog

    private lateinit var viewModel: PromocodeFragmentViewModel
    private lateinit var sendReq: LiveData<String?>

    var sale: Int = 0
    private val price: Int = 16

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View = inflater.inflate(R.layout.fragment_payment, container, false)

        viewModel = ViewModelProvider(this).get(PromocodeFragmentViewModel::class.java)
        sendReq = viewModel.getRequestSend()

        saleTxt = rootView.findViewById(R.id.sale_price)
        priceTxt = rootView.findViewById(R.id.price_txt)

        payBtn = rootView.findViewById(R.id.pay_btn)
        payBtn.setOnClickListener {
            timeToStartCheckout()
        }

        loadDialog = Dialog(requireContext())
        loadDialog.setContentView(R.layout.fragment_load_check)

        return rootView
    }

    override fun onStart() {
        super.onStart()

        if (sale != 0) {
            saleTxt.visibility = View.VISIBLE
            priceTxt.text = "${price - (price * sale / 100)}.00 ₽"
        }
    }

    private fun timeToStartCheckout() {
        val paymentParameters = PaymentParameters(
            Amount(BigDecimal.valueOf(price - (price * sale / 100).toDouble()), Currency.getInstance("RUB")),
            getString(R.string.title_item),
            "8 онлайн занятий йоги с инструктором",
            getString(R.string.yookassa_sdk_key),
            getString(R.string.yookassa_id_magazine),
            SavePaymentMethod.OFF,
            setOf(PaymentMethodType.BANK_CARD, PaymentMethodType.SBERBANK)
        )

        val uiParameters = UiParameters(
            false,
            ColorScheme(getPrimaryColor())
        )

        val intent: Intent = Checkout.createTokenizeIntent(requireContext(), paymentParameters, uiParameters = uiParameters)
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_TOKENIZE) {
            when (resultCode) {
                RESULT_OK -> {
                    if (data != null) {
                        val result: TokenizationResult = Checkout.createTokenizationResult(data)

                        Log.d("payData", result.paymentMethodType.name().decapitalize(Locale.ROOT) + " " + result.paymentToken)
                        val pay = Payment(
                            this,
                            result.paymentToken,
                            result.paymentMethodType.name().decapitalize(Locale.ROOT),
                            price - (price * sale / 100).toDouble()
                        )

                        pay.sendRequest()
                        sendReq.observe(this) {
                            if (it == null) {
                                loadDialog.show()
                            } else {
                                loadDialog.dismiss()
                                Log.d("payDataIt", it)
                                if (it.contains("Error")) {
                                    this.dismiss()
                                } else {
                                    Log.d("payData", "3ds $it")
                                    timeToStart3DS(it)
                                }

                                sendReq.removeObservers(viewLifecycleOwner)
                            }
                        }
                    }
                }
                RESULT_CANCELED -> { }
            }
        }

        if (resultCode == 1760) {
            when (resultCode) {
                RESULT_OK -> {
                    this.dismiss()
                }
                RESULT_CANCELED -> { }
            }
        }
    }

    private fun timeToStart3DS(url: String) {
        val intent: Intent = Checkout.create3dsIntent(
            requireContext(),
            url
        );
        startActivityForResult(intent, 1760);
    }

    private fun getPrimaryColor() : Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)

        return typedValue.data
    }

}