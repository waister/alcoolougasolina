package br.com.gazoza.alcoolougasolina.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.gazoza.alcoolougasolina.domain.Comparison

@Dao
interface ComparisonDao {

    @Query("SELECT * FROM comparisons ORDER BY timestamp DESC")
    fun getAllComparisons(): List<Comparison>

    @Query("SELECT * FROM comparisons ORDER BY timestamp DESC LIMIT 1")
    fun getLastComparison(): Comparison?

    @Query("SELECT MAX(id) FROM comparisons")
    fun getMaxId(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(comparison: Comparison): Long

    @Query("DELETE FROM comparisons")
    fun deleteAll()

    @Query("SELECT * FROM comparisons WHERE priceEthanol = :priceEthanol AND priceGasoline = :priceGasoline ORDER BY timestamp DESC LIMIT 1")
    fun getComparisonByPrices(priceEthanol: String, priceGasoline: String): Comparison?
}
