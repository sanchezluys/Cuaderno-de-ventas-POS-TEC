package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventas")
data class Venta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fechaHora: Long = System.currentTimeMillis(),
    val vendedorNombre: String,
    val total: Double,
    val estado: VentaEstado = VentaEstado.ACTIVA,
    val anuladaEn: Long? = null,
    val pagaCon: Double? = null,
    val vueltos: Double? = null,
    val clienteNombre: String? = null,
    val clienteTelefono: String? = null
)

