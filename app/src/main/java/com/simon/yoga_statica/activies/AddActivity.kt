package com.simon.yoga_statica.activies

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simon.yoga_statica.R
import com.simon.yoga_statica.fragments.FavoriteListFragment

class AddActivity : AppCompatActivity() {

    private var edit = false

    private lateinit var addTitle: EditText
    private lateinit var addShortAsuns: EditText
    private lateinit var addLongAsuns: EditText

    private lateinit var auth: FirebaseAuth

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_THEME = "theme"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)

        if (!prefs.contains(APP_PREFERENCES_THEME) || auth.currentUser == null) {
            setTheme(R.style.AppTheme)
        } else {
            when (prefs.getString(APP_PREFERENCES_THEME, "default")) {
                "coffee" -> setTheme(R.style.CoffeeAppTheme)
                "default" -> setTheme(R.style.AppTheme)
            }
        }
        setContentView(R.layout.activity_add)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        addTitle = findViewById(R.id.addTitle)
        addShortAsuns = findViewById(R.id.addShortAsuns)
        addLongAsuns = findViewById(R.id.addLongAsuns)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                edit = true
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        }

        addTitle.addTextChangedListener(textWatcher)
        addShortAsuns.addTextChangedListener(textWatcher)
        addLongAsuns.addTextChangedListener(textWatcher)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {

        if (edit) {
            AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Завершение добавления асуны")
                .setMessage("Вы уверены, что хотите завершить добавление?")
                .setPositiveButton("Завершить") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("Нет") { _, _ ->
                }
                .show()
        } else {
            super.onBackPressed()
        }
    }
}