package br.com.gazoza.alcoolougasolina.domain

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Comparison : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var priceEthanol: String = ""
    var priceGasoline: String = ""
    var proportion: Double = 0.0
    var percentage: String = ""
    var timestamp: Long = 0
}
