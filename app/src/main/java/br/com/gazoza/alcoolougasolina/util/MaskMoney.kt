package br.com.gazoza.alcoolougasolina.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

import java.text.NumberFormat

class MaskMoney(private val editText: EditText) : TextWatcher {
    private var isUpdating = false

    // Get the system money format. Example: Brazil (R$), USA ($)...
    private val numberFormat = NumberFormat.getCurrencyInstance()

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, after: Int) {
        // Prevent infinity loop
        if (isUpdating) {
            isUpdating = false
            return
        }

        isUpdating = true

        // Value from EditText
        var value = charSequence.toString()

        // Only numbers here
        value = value.replace("\\D+".toRegex(), "")

        try {
            // Format the value and put back on EditText
            value = numberFormat.format(java.lang.Double.parseDouble(value) / 100)
            value = value.replace("$", "$ ")

            editText.setText(value)
            editText.setSelection(value.length)
        } catch (ignored: NumberFormatException) {
        }

    }

    override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(editable: Editable) {}
}