package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val precioReferencia: Double,
    val categoria: String = "Accesorios & Fundas",
    val icono: String = "",
    val activo: Boolean = true
)


