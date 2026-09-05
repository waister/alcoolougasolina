package br.com.gazoza.alcoolougasolina.data.repository

import br.com.gazoza.alcoolougasolina.data.ComparisonDao
import br.com.gazoza.alcoolougasolina.domain.Comparison
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getAllComparisons(): Flow<List<Comparison>>

    suspend fun getLastComparison(): Comparison?

    suspend fun insertOrUpdate(comparison: Comparison): Long

    suspend fun deleteAll()

    suspend fun getComparisonByPrices(priceEthanol: String, priceGasoline: String): Comparison?
}

class HistoryRepositoryImpl(private val comparisonDao: ComparisonDao) : HistoryRepository {
    override fun getAllComparisons(): Flow<List<Comparison>> = comparisonDao.getAllComparisons()

    override suspend fun getLastComparison(): Comparison? = comparisonDao.getLastComparison()

    override suspend fun insertOrUpdate(comparison: Comparison): Long = comparisonDao.insertOrUpdate(comparison)

    override suspend fun deleteAll() {
        comparisonDao.deleteAll()
    }

    override suspend fun getComparisonByPrices(priceEthanol: String, priceGasoline: String): Comparison? =
        comparisonDao.getComparisonByPrices(priceEthanol, priceGasoline)
}
