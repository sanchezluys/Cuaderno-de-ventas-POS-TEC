package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "venta_items",
    foreignKeys = [
        ForeignKey(
            entity = Venta::class,
            parentColumns = ["id"],
            childColumns = ["ventaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ventaId")]
)
data class VentaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ventaId: Int,
    val productoNombre: String,
    val precioUnitario: Double,
    val cantidad: Int,
    val subtotal: Double = precioUnitario * cantidad
)
