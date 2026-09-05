package br.com.gazoza.alcoolougasolina.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.gazoza.alcoolougasolina.domain.Comparison
import kotlinx.coroutines.flow.Flow

@Dao
interface ComparisonDao {
    @Query("SELECT * FROM comparisons ORDER BY timestamp DESC")
    fun getAllComparisons(): Flow<List<Comparison>>

    @Query("SELECT * FROM comparisons ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastComparison(): Comparison?

    @Query("SELECT MAX(id) FROM comparisons")
    suspend fun getMaxId(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(comparison: Comparison): Long

    @Query("DELETE FROM comparisons")
    suspend fun deleteAll()

    @Query(
        "SELECT * FROM comparisons WHERE priceEthanol = :priceEthanol AND priceGasoline = :priceGasoline ORDER BY timestamp DESC LIMIT 1"
    )
    suspend fun getComparisonByPrices(priceEthanol: String, priceGasoline: String): Comparison?
}
