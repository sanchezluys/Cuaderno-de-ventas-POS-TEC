package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perfil_local")
data class PerfilLocal(
    @PrimaryKey
    val id: Int = 1,
    val tiendaNombre: String = "Tienda de Tecnología",
    val vendedorNombre: String = "Vendedor",
    val direccion: String = "",
    val telefono: String = "",
    val separadorMilesActivo: Boolean = true,
    val separadorMilesCaracter: String = ".", // "." (ej: $ 45.000) o "," (ej: $ 45,000)
    val logoUri: String? = null // Ruta de archivo o URI del logo del local
)

