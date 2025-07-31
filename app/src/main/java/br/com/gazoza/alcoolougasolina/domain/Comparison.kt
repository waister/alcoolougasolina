package br.com.gazoza.alcoolougasolina.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comparisons")
data class Comparison(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var priceEthanol: String = "",
    var priceGasoline: String = "",
    var proportion: Double = 0.0,
    var percentage: String = "",
    var timestamp: Long = 0
)
