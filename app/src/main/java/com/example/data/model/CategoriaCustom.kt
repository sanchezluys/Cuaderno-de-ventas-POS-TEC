package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias_custom")
data class CategoriaCustom(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String
)
