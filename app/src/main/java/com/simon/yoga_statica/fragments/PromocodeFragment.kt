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

    private lateinit var promocode: TextView

    private lateinit var copyPromocode: ImageButton
    private lateinit var infoBtn: ImageButton
    private lateinit var payBtn: Button
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
}