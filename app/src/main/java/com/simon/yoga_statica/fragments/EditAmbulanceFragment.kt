package com.simon.yoga_statica.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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

class EditAmbulanceFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    private lateinit var weight: EditText
    private lateinit var height: EditText
    private lateinit var press: EditText
    private lateinit var sugar: EditText

    private lateinit var btnSave: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_ambulance, container, false)

        auth = Firebase.auth

        weight = root.findViewById(R.id.weight_txt)
        height = root.findViewById(R.id.height_txt)
        press = root.findViewById(R.id.press_txt)
        sugar = root.findViewById(R.id.sugar_txt)

        btnSave = root.findViewById(R.id.save)

        btnSave.setOnClickListener {

        }

        return root
    }

    private fun addNewData() {

        CoroutineScope(Dispatchers.IO).launch {
            auth.currentUser?.let {
                val id = Ambulance().getId(auth.currentUser!!.uid)
                if (id != null) {
                    val data = Ambulance().getData(id)

                    val weight = data.getString("width")
                    val height = data.getString("height")
                    val press = data.getString("press")
                    val sugar = data.getString("sugar")

                    val json =
                        "{\"weight\": $weight, \"height\": $height, \"press\": $press, \"sugar\": $sugar}"

                    Ambulance().setData(json, id)
                } else {
                    val weight = weight_txt.text
//                    val height = data.getString("height")
//                    val press = data.getString("press")
//                    val sugar = data.getString("sugar")

                    val json =
                        "{\"weight\": $weight, \"height\": $height, \"press\": $press, \"sugar\": $sugar}"

                    Ambulance().setData(json)
                }
            }

        }

    }

}