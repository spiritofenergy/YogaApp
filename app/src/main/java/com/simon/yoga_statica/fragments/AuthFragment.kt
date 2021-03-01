package com.simon.yoga_statica.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.activies.MainActivity
import com.simon.yoga_statica.adapters.AuthAdapter
import com.simon.yoga_statica.classes.Payment
import com.simon.yoga_statica.viewmodels.PromocodeFragmentViewModel
import ru.yoo.sdk.kassa.payments.*
import java.math.BigDecimal
import java.util.*

class AuthFragment : Fragment() {

    private val REQUEST_CODE_TOKENIZE = 1234

    private val db = Firebase.firestore
    private lateinit var gso: GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var seminarBtn: Button

    private val auth = Firebase.auth

    private lateinit var signInGoogle: Button

    private lateinit var loadDialog: Dialog

    private lateinit var viewModel: PromocodeFragmentViewModel
    private lateinit var sendReq: LiveData<String?>

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_AUTH = "authWithin"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_auth, container, false)

        loadDialog = Dialog(requireContext())
        loadDialog.setContentView(R.layout.fragment_load_check)

        viewModel = ViewModelProvider(this).get(PromocodeFragmentViewModel::class.java)
        sendReq = viewModel.getRequestSend()

        seminarBtn = rootView.findViewById(R.id.order_btn_seminar)

        prefs = activity?.getSharedPreferences("settings", Context.MODE_PRIVATE)!!

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        callbackManager = CallbackManager.Factory.create()

        signInGoogle = rootView.findViewById(R.id.google_signIn_auth)
        signInGoogle.setOnClickListener {
            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, 123)
        }

        seminarBtn.setOnClickListener {
            timeToStartCheckout("Семинар \"Погоня за дофамином\"")
        }

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }

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
                        .setTitle(getString(R.string.success_pay))
                        .setMessage(getString(R.string.message_payment_2))
                        .create()
                    alert.show()
                }
                Activity.RESULT_CANCELED -> {
                    val alert = AlertDialog.Builder(requireContext())
                        .setMessage(getString(R.string.payment_error))
                        .create()
                    alert.show()
                }
            }
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            Log.d("login", "signInWithEmail:success")
            firebaseAuthWithGoogle(account?.idToken.toString())
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(
                "err",
                "signInResult:failed code=" + e.statusCode
            )
            Toast.makeText(
                activity, getString(R.string.auth_fail),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("login", "signInWithCredential:success")

                    val user = auth.currentUser
                    addUserToDatabase(user)

                    openMain()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("login", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        activity, getString(R.string.auth_fail),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
    }

    private fun addUserToDatabase(currentUser: FirebaseUser?) {
        val user = hashMapOf(
            "id" to currentUser?.uid.toString().trim(),
            "root" to "user",
            "countAsuns" to 0,
            "status" to 1
        )

        db.collection("users")
            .whereEqualTo("id", currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty)
                    db.collection("users")
                        .add(user)
            }
            .addOnFailureListener { exception ->
                Log.w("home", "Error getting documents: ", exception)
            }
    }

    private fun openMain() {
        val intent = Intent(
            activity,
            MainActivity::class.java
        )
        startActivity(intent)
        activity?.finish()
    }

    private fun timeToStartCheckout(title: String) {

        val paymentParameters = PaymentParameters(
            Amount(BigDecimal.valueOf(750), Currency.getInstance("RUB")),
            title,
            "Посещение семинара \"Погоня за дофамином\"",
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