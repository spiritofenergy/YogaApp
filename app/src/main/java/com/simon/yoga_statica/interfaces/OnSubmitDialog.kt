package com.simon.yoga_statica.interfaces

import android.content.DialogInterface
import android.widget.EditText

interface OnSubmitDialog {
    fun setOnClickPositive(view: EditText, dialog: DialogInterface, which: Int)
}