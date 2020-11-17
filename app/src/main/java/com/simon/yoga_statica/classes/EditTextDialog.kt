package com.simon.yoga_statica.classes

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import com.simon.yoga_statica.R
import com.simon.yoga_statica.interfaces.OnSubmitDialog

class EditTextDialog(
    private val ctx: Context,
    private val clickView: TextView,
    private val title: String,
    private val message: String,
    private val default: String?,
    private val multiline: Boolean,
    private val listener: OnSubmitDialog
    ) : AlertDialog.Builder(ctx) {

    private lateinit var view: EditText
    private lateinit var alert: AlertDialog

    override fun show(): AlertDialog {
        view = EditText(ctx)

        if (!multiline) {
            view.isSingleLine = true
            view.inputType = InputType.TYPE_CLASS_TEXT
        }

        if (default != null)
            view.append(default)

        setView(view)
        setTitle(title)
        setMessage(message)
        setPositiveButton("Ok") { dialog, which ->
            if (view.text.toString() != "") {
                listener.setOnClickPositive(view, dialog, which)

                if (Build.VERSION.SDK_INT >= 23)
                    clickView.setTextColor(ctx.getColor(R.color.colorTextTitle))
                else
                    clickView.setTextColor(ctx.resources.getColor(R.color.colorTextTitle))
            } else
                if (default == null)
                    clickView.setTextColor(Color.RED)

        }
        setNegativeButton("Cancel") { _, _ ->
            clickView.setTextColor(Color.RED)
        }

        setOnCancelListener {
            clickView.setTextColor(Color.RED)
        }

        alert = super.show()
        return alert
    }


}