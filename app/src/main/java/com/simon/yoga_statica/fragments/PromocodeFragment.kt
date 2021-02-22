package com.simon.yoga_statica.fragments

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.simon.yoga_statica.R
import com.simon.yoga_statica.classes.Payment
import com.simon.yoga_statica.classes.Promocode
import com.simon.yoga_statica.viewmodels.PromocodeFragmentViewModel
import ru.yoo.sdk.kassa.payments.*
import java.math.BigDecimal
import java.util.*


class PromocodeFragment : Fragment() {

    private val REQUEST_CODE_TOKENIZE = 1234

    private lateinit var promocode: TextView

    private lateinit var copyPromocode: ImageButton
    private lateinit var infoBtn: ImageButton
    private lateinit var payBtn: Button
    private lateinit var seminarBtn: Button
    private lateinit var promoTxt: EditText
    private lateinit var useBtn: Button
    private lateinit var saleTxt: TextView
    private lateinit var balanceTxt: TextView
    private lateinit var promoTitleLbl: TextView
    private lateinit var promoCountTxt: TextView

    private lateinit var viewModel: PromocodeFragmentViewModel
    private lateinit var checkPromocode: LiveData<Boolean>
    private lateinit var promocodeLiveData: LiveData<String>
    private lateinit var saleLiveData: LiveData<Int?>
    private lateinit var balanceLiveData: LiveData<Double?>
    private lateinit var promoLiveData: LiveData<String?>
    private lateinit var countLiveData: LiveData<Int>
    private lateinit var sendReq: LiveData<String?>

    private lateinit var loadDialog: Dialog

    private var sale: Int = 0

    private val onClickCopy = View.OnClickListener {
        val clipboardManager: ClipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("promocode", promocode.text.toString())

        clipboardManager.setPrimaryClip(clip)

        Toast
            .makeText(
                context,
                "Промокод скопирован",
                Toast.LENGTH_SHORT
            )
            .show()

        copyPromocode.isEnabled = false
        copyPromocode.setImageResource(R.drawable.ic_baseline_check_24)
        copyPromocode.setBackgroundResource(R.drawable.button_shadow_disable)

        promocode.isEnabled = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View = inflater.inflate(R.layout.fragment_promocode, container, false)

        activity?.title = getString(R.string.pay_fragment_title)

        loadDialog = Dialog(requireContext())
        loadDialog.setContentView(R.layout.fragment_load_check)

        viewModel = ViewModelProvider(this).get(PromocodeFragmentViewModel::class.java)

        checkPromocode = viewModel.promocodeIsExist()
        checkPromocode.observe(viewLifecycleOwner, { it ->
            Log.d("isExistPromocode", it.toString())

            if (it == false) {
                promocode.text = Promocode().createAndSavePromocode()
            } else {
                promocodeLiveData = viewModel.getPromocode()
                promocodeLiveData.observe(viewLifecycleOwner, { _promocode ->
                    promocode.text = _promocode
                })
            }

        })

        countLiveData = viewModel.getCount()
        countLiveData.observe(viewLifecycleOwner, { count ->
            promoCountTxt.text = count.toString()
        })

        sendReq = viewModel.getRequestSend()

        promoLiveData = viewModel.getUsesPromo()
        promoLiveData.observe(viewLifecycleOwner, {
            if (it != null) {
                promoTitleLbl.text = getString(R.string.promo_title_after)
                promoTxt.setText(it)
                promoTxt.isEnabled = false
                useBtn.isEnabled = false
            }
        })

        promocode = rootView.findViewById(R.id.promocode)
        copyPromocode = rootView.findViewById(R.id.copy_promocode_btn)
        infoBtn = rootView.findViewById(R.id.info_promocode_btn)
        payBtn = rootView.findViewById(R.id.order_btn)
        saleTxt = rootView.findViewById(R.id.sale_txt)
        balanceTxt = rootView.findViewById(R.id.balance_txt)
        useBtn = rootView.findViewById(R.id.use_btn)
        promoTxt = rootView.findViewById(R.id.promo_txt)
        promoTitleLbl = rootView.findViewById(R.id.promo_title_lbl)
        promoCountTxt = rootView.findViewById(R.id.promo_count_txt)
        seminarBtn = rootView.findViewById(R.id.order_btn_seminar)

        copyPromocode.setOnClickListener(onClickCopy)
        promocode.setOnClickListener(onClickCopy)
        infoBtn.setOnClickListener {
            openInfo()
        }

        payBtn.setOnClickListener {
            val dialog = PaymentDialogFragment()
            dialog.sale = sale
            dialog.show(childFragmentManager, "pay")
        }

        useBtn.setOnClickListener {
            if (promoTxt.text.isNotEmpty() && promoTxt.text.toString() != promocode.text.toString()) {
                val setPromo = viewModel.setPromocode(promoTxt.text.toString())
                setPromo.observe(viewLifecycleOwner, {

                    if (it == null) {
                        loadDialog.show()
                    } else {
                        loadDialog.dismiss()
                        if (it == false) {
                            promoTxt.setTextColor(Color.RED)
                            Handler(Looper.getMainLooper()).postDelayed({
                                promoTxt.setTextColor(Color.BLACK)
                            }, 4000)
                        } else {
                            promoTitleLbl.text = getString(R.string.promo_title_after)
                            promoTxt.isEnabled = false
                            useBtn.isEnabled = false
                        }

                        setPromo.removeObservers(viewLifecycleOwner)
                    }
                })
            }
        }

        seminarBtn.setOnClickListener {
            timeToStartCheckout("Семинар \"Дофамин\"")
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()

        saleLiveData = viewModel.getSale()
        saleLiveData.observe(viewLifecycleOwner, {
            if (it == null) {
                saleTxt.text = "0 %"
                sale = 0
            } else {
                saleTxt.text = "$it %"
                sale = it
            }
        })

        balanceLiveData = viewModel.getBalance()
        balanceLiveData.observe(viewLifecycleOwner, {
            if (it == null) {
                balanceTxt.text = "0 ₽"
            } else {
                balanceTxt.text = "$it ₽"
            }
        })
    }

    private fun openInfo() {
        val dialog = InfoPromocodeFragment()

        dialog.show(childFragmentManager, "info")
    }

    private fun timeToStartCheckout(title: String) {

        val paymentParameters = PaymentParameters(
            Amount(BigDecimal.valueOf(750), Currency.getInstance("RUB")),
            title,
            "Посещение семинара \"Дофамин\"",
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
                Activity.RESULT_OK -> {
                    if (data != null) {
                        val result: TokenizationResult = Checkout.createTokenizationResult(data)

                        Log.d("payData", result.paymentMethodType.name().decapitalize(Locale.ROOT) + " " + result.paymentToken)
                        val pay = Payment(
                            this,
                            result.paymentToken,
                            result.paymentMethodType.name().decapitalize(Locale.ROOT),
                            750.toDouble()
                        )

                        pay.sendRequest()
                        sendReq.observe(this) {
                            if (it == null) {
                                loadDialog.show()
                            } else {
                                loadDialog.dismiss()
                                Log.d("payDataIt", it)
                                if (!it.contains("Error")) {
                                    Log.d("payData", "3ds $it")
                                    timeToStart3DS(it)
                                }

                                sendReq.removeObservers(viewLifecycleOwner)
                            }
                        }
                    }
                }
                Activity.RESULT_CANCELED -> { }
            }
        }

        if (requestCode == 1760) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val alert = AlertDialog.Builder(requireContext())
                        .setTitle("Платеж прошел успешно")
                        .setMessage("Вы успешно оплатили семинар.")
                        .create()
                    alert.show()
                }
                Activity.RESULT_CANCELED -> {
                    val alert = AlertDialog.Builder(requireContext())
                        .setMessage("Платеж отменен")
                        .create()
                    alert.show()
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