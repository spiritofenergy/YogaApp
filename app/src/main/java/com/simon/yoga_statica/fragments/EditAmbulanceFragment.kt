package com.simon.yoga_statica.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.simon.yoga_statica.R
import com.simon.yoga_statica.classes.Ambulance
import kotlinx.android.synthetic.main.fragment_ambulance_edit.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditAmbulanceFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    private lateinit var weightTxt: EditText
    private lateinit var heightTxt: EditText
    private lateinit var pressTxt: EditText
    private lateinit var sugarTxt: EditText

    private lateinit var btnSave: Button
    private lateinit var trueReq: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_ambulance_edit, container, false)

        auth = Firebase.auth

        weightTxt = root.findViewById(R.id.weight_txt)
        heightTxt = root.findViewById(R.id.height_txt)
        pressTxt = root.findViewById(R.id.press_txt)
        sugarTxt = root.findViewById(R.id.sugar_txt)

        btnSave = root.findViewById(R.id.save)
        trueReq = root.findViewById(R.id.true_req)

        btnSave.setOnClickListener {
            addNewData()
        }

        return root
    }

    private fun addNewData() {

        CoroutineScope(Dispatchers.IO).launch {
            auth.currentUser?.let {
                println("HELLO")
                val id = Ambulance().getId(auth.currentUser!!.uid)
                println(id)
                if (id != null) {
                    val data = Ambulance().getData(id)

                    println(data)

                    var weight = data.getString("weight")
                    if (weightTxt.text.isNotEmpty()) {
                        weight += ", ${weightTxt.text}"
                    }
                    var height = data.getString("height")
                    if (heightTxt.text.isNotEmpty()) {
                        height += ", ${heightTxt.text}"
                    }
                    var press = data.getString("press")
                    if (pressTxt.text.isNotEmpty()) {
                        press += ", ${pressTxt.text}"
                    }
                    var sugar = data.getString("sugar")
                    if (sugarTxt.text.isNotEmpty()) {
                        sugar += ", ${sugarTxt.text}"
                    }

                    val json =
                        "{\"weight\": \"$weight\", \"height\": \"$height\", \"press\": \"$press\", \"sugar\": \"$sugar\"}"

                    println(json)

                    Ambulance().setData(json, id)
                    withContext(Dispatchers.Main) {
                        parentFragmentManager.popBackStack()
                    }
                } else {
                    val weight = weightTxt.text
                    val height = heightTxt.text
                    val press = pressTxt.text
                    val sugar = sugarTxt.text

                    val json =
                        "{\"weight\": \"$weight\", \"height\": \"$height\", \"press\": \"$press\", \"sugar\": \"$sugar\"}"

                    Ambulance().setData(json, null, auth.currentUser!!.uid)
                    withContext(Dispatchers.Main) {
                        parentFragmentManager.popBackStack()
                    }
                }
            }

        }

    }

}