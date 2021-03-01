package com.simon.yoga_statica.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.simon.yoga_statica.R
import com.simon.yoga_statica.classes.Ambulance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AmbulanceFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private lateinit var weightGraph: GraphView
    private lateinit var weightTxt: TextView
    private lateinit var heightGraph: GraphView
    private lateinit var heightTxt: TextView
    private lateinit var pressGraph: GraphView
    private lateinit var pressTxt: TextView
    private lateinit var sugarGraph: GraphView
    private lateinit var sugarTxt: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_ambulance, container, false)

        auth = Firebase.auth

        weightGraph = root.findViewById(R.id.weight_graph)
        weightTxt = root.findViewById(R.id.weight_last_data)
        heightGraph = root.findViewById(R.id.height_graph)
        heightTxt = root.findViewById(R.id.height_last_data)
        pressGraph = root.findViewById(R.id.press_graph)
        pressTxt = root.findViewById(R.id.press_last_data)
        sugarGraph = root.findViewById(R.id.sugar_graph)
        sugarTxt = root.findViewById(R.id.sugar_last_data)


        root.findViewById<ImageButton>(R.id.open_edit_fm).setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            with(transaction) {
                replace(R.id.fragmentContainer, EditAmbulanceFragment())
                addToBackStack(null)
                commit()
            }
        }

        return root
    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.IO).launch {
            val id = Ambulance().getId(auth.currentUser!!.uid)
            id?.let {
                val data = Ambulance().getData(id)

                val weight = data.getString("weight").split(", ").filter { it.isNotEmpty() }.map { it.toInt() }
                val height = data.getString("height").split(", ").filter { it.isNotEmpty() }.map { it.toInt() }
                val press = data.getString("press").split(", ").filter { it.isNotEmpty() }.map { it.toInt() }
                val sugar = data.getString("sugar").split(", ").filter { it.isNotEmpty() }.map { it.toInt() }

                var dataWeight = arrayOf<DataPoint>()
                for ((i, w) in weight.withIndex()) {
                    dataWeight += DataPoint(i.toDouble(), w.toDouble())
                }

                var dataHeight = arrayOf<DataPoint>()
                for ((i, h) in height.withIndex()) {
                    dataHeight += DataPoint(i.toDouble(), h.toDouble())
                }

                var dataPress = arrayOf<DataPoint>()
                for ((i, p) in press.withIndex()) {
                    dataPress += DataPoint(i.toDouble(), p.toDouble())
                }

                var dataSugar = arrayOf<DataPoint>()
                for ((i, s) in sugar.withIndex()) {
                    dataSugar += DataPoint(i.toDouble(), s.toDouble())
                }

                val weightSeries = LineGraphSeries(dataWeight)
                val heightSeries = LineGraphSeries(dataHeight)
                val pressSeries = LineGraphSeries(dataPress)
                val sugarSeries = LineGraphSeries(dataSugar)
                withContext(Dispatchers.Main) {
                    weightGraph.addSeries(weightSeries)
                    heightGraph.addSeries(heightSeries)
                    pressGraph.addSeries(pressSeries)
                    sugarGraph.addSeries(sugarSeries)

                    weightTxt.text = if (!weight.isNullOrEmpty()) weight.last().toString() else getString(
                                            R.string.error_data)
                    heightTxt.text = if (!height.isNullOrEmpty()) height.last().toString() else getString(
                        R.string.error_data)
                    pressTxt.text = if (!press.isNullOrEmpty()) press.last().toString() else getString(
                        R.string.error_data)
                    sugarTxt.text = if (!sugar.isNullOrEmpty()) sugar.last().toString() else getString(
                        R.string.error_data)
                }
            }

        }

        weightTxt
    }

}