package br.com.gazoza.alcoolougasolina.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MaskMoneyTest {

    @Test
    fun `given empty input, when formatted, then returns empty`() {
        assertEquals("", MaskMoney.format(""))
    }

    @Test
    fun `given numeric input, when formatted, then formats as BRL currency`() {
        val result = MaskMoney.format("328")
        assertEquals(3.28, MaskMoney.parse(result), 0.001)
    }

    @Test
    fun `given typing single digit on empty field, then formats as cents`() {
        val result = MaskMoney.formatMoneyInput(previousText = "", newText = "5")
        assertEquals(0.05, MaskMoney.parse(result), 0.001)
    }

    @Test
    fun `given typing multiple digits sequentially, then formats correctly`() {
        var text = ""
        text = MaskMoney.formatMoneyInput(previousText = text, newText = "5")
        text = MaskMoney.formatMoneyInput(previousText = text, newText = text + "9")
        text = MaskMoney.formatMoneyInput(previousText = text, newText = text + "0")

        assertEquals(5.90, MaskMoney.parse(text), 0.001)
    }

    @Test
    fun `given backspace on full formatted currency, then removes last digit and adjusts`() {
        val original = MaskMoney.format("590") // R$ 5,90
        val afterBackspace = MaskMoney.formatMoneyInput(previousText = original, newText = original.dropLast(1))

        assertEquals(0.59, MaskMoney.parse(afterBackspace), 0.001)
    }

    @Test
    fun `given backspace on non-digit character like space or dollar sign, then drops last digit`() {
        val original = MaskMoney.format("590")
        // simulating deletion of a non-digit character from inside the string
        val afterNonDigitDeleted = MaskMoney.formatMoneyInput(previousText = original, newText = original.replace("$", ""))

        assertEquals(0.59, MaskMoney.parse(afterNonDigitDeleted), 0.001)
    }

    @Test
    fun `given backspacing all characters, then returns empty`() {
        var text = MaskMoney.format("5") // R$ 0,05
        text = MaskMoney.formatMoneyInput(previousText = text, newText = "")

        assertEquals("", text)
    }

    @Test
    fun `given parse with various currency inputs, then parses correct double`() {
        assertEquals(3.28, MaskMoney.parse("R$ 3,28"), 0.001)
        assertEquals(5.90, MaskMoney.parse("R$ 5,90"), 0.001)
        assertEquals(0.0, MaskMoney.parse(""), 0.001)
    }
}
