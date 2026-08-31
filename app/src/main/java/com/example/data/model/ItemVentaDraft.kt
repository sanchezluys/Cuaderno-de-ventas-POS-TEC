package com.example.data.model

import java.util.UUID

data class ItemVentaDraft(
    val draftId: String = UUID.randomUUID().toString(),
    val productoCatalogoId: Int? = null,
    val productoNombre: String,
    val precioUnitario: Double,
    val cantidad: Int = 1
) {
    val subtotal: Double
        get() = precioUnitario * cantidad
}
