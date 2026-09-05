package br.com.gazoza.alcoolougasolina.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import br.com.gazoza.alcoolougasolina.utils.BaseRobolectricTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PreferencesRepositoryTest : BaseRobolectricTest() {
    private lateinit var repository: PreferencesRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = PreferencesRepositoryImpl(context)
        repository.clear()
    }

    @Test
    fun `test price storage and retrieval`() {
        repository.setLastEthanolPrice("3.50")
        repository.setLastGasolinePrice("5.50")

        assertEquals("3.50", repository.getLastEthanolPrice())
        assertEquals("5.50", repository.getLastGasolinePrice())
    }

    @Test
    fun `test deviceId storage and retrieval`() {
        repository.setDeviceId("987654321")
        repository.setDeviceIdOld("123456789")

        assertEquals("987654321", repository.getDeviceId())
        assertEquals("123456789", repository.getDeviceIdOld())
    }

    @Test
    fun `test clear resets preferences`() {
        repository.setLastEthanolPrice("3.50")
        repository.clear()

        assertEquals("", repository.getLastEthanolPrice())
    }
}
