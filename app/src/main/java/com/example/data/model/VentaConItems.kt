package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class VentaConItems(
    @Embedded
    val venta: Venta,
    @Relation(
        parentColumn = "id",
        entityColumn = "ventaId"
    )
    val items: List<VentaItem>
)
