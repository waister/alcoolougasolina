package br.com.gazoza.alcoolougasolina.util

import java.text.NumberFormat
import java.util.Locale

object MaskMoney {
    fun format(input: String): String {
        val digits = input.filter { it.isDigit() }.trimStart('0')
        if (digits.isEmpty()) return ""
        val limited = if (digits.length > 6) digits.take(6) else digits
        val value = limited.toDouble() / 100.0
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(value)
    }

    fun formatMoneyInput(previousText: String, newText: String): String {
        if (newText.isEmpty()) return ""

        val prevDigits = previousText.filter { it.isDigit() }
        val newDigits = newText.filter { it.isDigit() }

        val digitsToUse: String =
            when {
                newText.length < previousText.length -> {
                    if (newDigits.length < prevDigits.length) {
                        newDigits
                    } else if (prevDigits.isNotEmpty()) {
                        prevDigits.dropLast(1)
                    } else {
                        ""
                    }
                }
                else -> {
                    if (newDigits.length > 6) newDigits.take(6) else newDigits
                }
            }

        val cleanDigits = digitsToUse.trimStart('0')
        if (cleanDigits.isEmpty()) {
            return ""
        }

        val parsed = cleanDigits.toDouble() / 100.0
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(parsed)
    }

    fun parse(formatted: String): Double {
        val digits = formatted.filter { it.isDigit() }
        if (digits.isEmpty()) return 0.0
        return digits.toDouble() / 100.0
    }
}
