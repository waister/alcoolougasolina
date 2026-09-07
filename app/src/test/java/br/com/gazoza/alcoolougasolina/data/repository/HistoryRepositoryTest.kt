package br.com.gazoza.alcoolougasolina.data.repository

import br.com.gazoza.alcoolougasolina.data.ComparisonDao
import br.com.gazoza.alcoolougasolina.domain.Comparison
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HistoryRepositoryTest {
    private val comparisonDao: ComparisonDao = mockk()
    private lateinit var repository: HistoryRepository

    @Before
    fun setup() {
        repository = HistoryRepositoryImpl(comparisonDao)
    }

    @Test
    fun `when getAllComparisons is called, then delegates to comparisonDao`() = runTest {
        val list = listOf(Comparison(id = 1, priceEthanol = "3.0", priceGasoline = "5.0"))
        every { comparisonDao.getAllComparisons() } returns flowOf(list)

        val result = repository.getAllComparisons().first()

        assertEquals(list, result)
    }

    @Test
    fun `when insertOrUpdate is called, then delegates to comparisonDao`() = runTest {
        val item = Comparison(id = 1, priceEthanol = "3.0", priceGasoline = "5.0")
        coEvery { comparisonDao.insertOrUpdate(item) } returns 1L

        val id = repository.insertOrUpdate(item)

        assertEquals(1L, id)
        coVerify { comparisonDao.insertOrUpdate(item) }
    }

    @Test
    fun `when deleteAll is called, then delegates to comparisonDao`() = runTest {
        coEvery { comparisonDao.deleteAll() } returns Unit

        repository.deleteAll()

        coVerify { comparisonDao.deleteAll() }
    }
}
