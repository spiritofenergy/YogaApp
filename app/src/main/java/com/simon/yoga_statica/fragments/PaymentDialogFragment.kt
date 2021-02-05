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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getColor
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

    private lateinit var saleIndividTxt: TextView
    private lateinit var priceIndividTxt: TextView
    private lateinit var payIndividBtn: Button

    private lateinit var saleVipTxt: TextView
    private lateinit var priceVipTxt: TextView
    private lateinit var payVipBtn: Button

    private lateinit var loadDialog: Dialog

    private lateinit var viewModel: PromocodeFragmentViewModel
    private lateinit var sendReq: LiveData<String?>

    private var usesPromo: String? = null
    var sale: Int = 0
    private val price: Int = 1600
    private val priceIndividal: Int = 2000
    private val priceVip: Int = 5000

    private var curPrice = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View = inflater.inflate(R.layout.fragment_payment, container, false)

        viewModel = ViewModelProvider(this).get(PromocodeFragmentViewModel::class.java)
        sendReq = viewModel.getRequestSend()

        val promo = viewModel.getUsesPromo()
        promo.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                usesPromo = it
            }
        }

        saleTxt = rootView.findViewById(R.id.sale_price)
        priceTxt = rootView.findViewById(R.id.price_txt)

        payBtn = rootView.findViewById(R.id.pay_btn)
        payBtn.setOnClickListener {
            timeToStartCheckout(getString(R.string.title_item), price)
        }

        saleIndividTxt = rootView.findViewById(R.id.sale_price_individ)
        priceIndividTxt = rootView.findViewById(R.id.price_txt_individ)

        payIndividBtn = rootView.findViewById(R.id.pay_btn_individ)
        payIndividBtn.setOnClickListener {
            timeToStartCheckout(getString(R.string.title_item_individ), priceIndividal)
        }

        saleVipTxt = rootView.findViewById(R.id.sale_price_vip)
        priceVipTxt = rootView.findViewById(R.id.price_txt_vip)

        payVipBtn = rootView.findViewById(R.id.pay_btn_vip)
        payVipBtn.setOnClickListener {
            timeToStartCheckout("${getString(R.string.title_item)} VIP", priceVip)
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

            saleIndividTxt.visibility = View.VISIBLE
            priceIndividTxt.text = "${priceIndividal - (priceIndividal * sale / 100)}.00 ₽"

            saleVipTxt.visibility = View.VISIBLE
            priceVipTxt.text = "${priceVip - (priceVip * sale / 100)}.00 ₽"
        }
    }

    private fun timeToStartCheckout(title: String, price: Int) {

        curPrice = price - (price * sale / 100)

        val paymentParameters = PaymentParameters(
            Amount(BigDecimal.valueOf(price - (price * sale / 100).toDouble()), Currency.getInstance("RUB")),
            title,
            "8 онлайн занятий йоги с инструктором",
            getString(R.string.yookassa_sdk_key),
            getString(R.string.yookassa_id_magazine),
            SavePaymentMethod.OFF,
            setOf(PaymentMethodType.BANK_CARD)
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

                        if (!usesPromo.isNullOrEmpty()) {
                            viewModel.setBalance(curPrice, usesPromo!!)
                        }

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

        if (requestCode == 1760) {
            when (resultCode) {
                RESULT_OK -> {
                    this.dismiss()

                    val alert = AlertDialog.Builder(requireContext())
                        .setTitle("Платеж прошел успешно")
                        .setMessage("Вы приобрели курс по йоге. В ближайшее время с вами свяжутся.")
                        .create()
                    alert.show()

                    if (!usesPromo.isNullOrEmpty()) {
                        viewModel.setBalance(curPrice, usesPromo!!)
                    }
                }
                RESULT_CANCELED -> {
                    val alert = AlertDialog.Builder(requireContext())
                        .setMessage("Платеж отменен")
                        .create()
                    alert.show()
                    this.dismiss()
                }
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