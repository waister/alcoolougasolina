package br.com.gazoza.alcoolougasolina.util

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class MaskMoneyTest {
    @Test
    fun `given empty input, when formatted, then returns empty`() {
        assertEquals("", MaskMoney.format(""))
    }

    @Test
    fun `given numeric input in pt-BR locale, when formatted, then formats with comma and R$`() {
        val ptLocale = Locale.forLanguageTag("pt-BR")
        val result = MaskMoney.format("328", ptLocale)
        assertEquals(3.28, MaskMoney.parse(result), 0.001)
        assert(result.contains(","))
    }

    @Test
    fun `given numeric input in US locale, when formatted, then formats with dot and dollar sign`() {
        val usLocale = Locale.US
        val result = MaskMoney.format("328", usLocale)
        assertEquals(3.28, MaskMoney.parse(result), 0.001)
        assert(result.contains("$"))
        assert(result.contains("3.28"))
    }

    @Test
    fun `given typing single digit on empty field in US locale, then formats as cents`() {
        val result = MaskMoney.formatMoneyInput(previousText = "", newText = "5", locale = Locale.US)
        assertEquals(0.05, MaskMoney.parse(result), 0.001)
        assert(result.contains("0.05"))
    }

    @Test
    fun `given typing multiple digits sequentially in US locale, then formats correctly`() {
        val usLocale = Locale.US
        var text = ""
        text = MaskMoney.formatMoneyInput(previousText = text, newText = "5", locale = usLocale)
        text = MaskMoney.formatMoneyInput(previousText = text, newText = text + "9", locale = usLocale)
        text = MaskMoney.formatMoneyInput(previousText = text, newText = text + "0", locale = usLocale)

        assertEquals(5.90, MaskMoney.parse(text), 0.001)
        assert(text.contains("5.90"))
    }

    @Test
    fun `given backspace on full formatted currency, then removes last digit and adjusts`() {
        val original = MaskMoney.format("590", Locale.US) // $5.90
        val afterBackspace = MaskMoney.formatMoneyInput(previousText = original, newText = original.dropLast(1), locale = Locale.US)

        assertEquals(0.59, MaskMoney.parse(afterBackspace), 0.001)
    }

    @Test
    fun `given backspace on non-digit character like space or dollar sign, then drops last digit`() {
        val original = MaskMoney.format("590", Locale.US)
        val afterNonDigitDeleted = MaskMoney.formatMoneyInput(previousText = original, newText = original.replace("$", ""), locale = Locale.US)

        assertEquals(0.59, MaskMoney.parse(afterNonDigitDeleted), 0.001)
    }

    @Test
    fun `given backspacing all characters, then returns empty`() {
        var text = MaskMoney.format("5", Locale.US)
        text = MaskMoney.formatMoneyInput(previousText = text, newText = "", locale = Locale.US)

        assertEquals("", text)
    }

    @Test
    fun `given parse with various currency inputs, then parses correct double`() {
        assertEquals(3.28, MaskMoney.parse("R$ 3,28"), 0.001)
        assertEquals(3.28, MaskMoney.parse("$3.28"), 0.001)
        assertEquals(5.90, MaskMoney.parse("R$ 5,90"), 0.001)
        assertEquals(5.90, MaskMoney.parse("$5.90"), 0.001)
        assertEquals(0.0, MaskMoney.parse(""), 0.001)
    }

    @Test
    fun `given getZeroCurrency for various locales, returns expected zero representation`() {
        val usZero = MaskMoney.getZeroCurrency(Locale.US)
        assert(usZero.contains("0.00"))

        val ptZero = MaskMoney.getZeroCurrency(Locale.forLanguageTag("pt-BR"))
        assert(ptZero.contains("0,00"))
    }
}
