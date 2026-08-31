package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.vector.ImageVector

object TechCategories {
    const val ALL = "Todos"
    const val PHONES = "Celulares"
    const val CABLES_CHARGERS = "Cables & Carga"
    const val AUDIO = "Audio & Sonido"
    const val ACCESSORIES = "Accesorios & Fundas"
    const val STORAGE = "Almacenamiento"
    const val TECH_SERVICE = "Servicio Técnico"
    const val OTHERS = "Otros"

    val list = listOf(
        PHONES,
        CABLES_CHARGERS,
        AUDIO,
        ACCESSORIES,
        STORAGE,
        TECH_SERVICE,
        OTHERS
    )

    val filterList = listOf(
        ALL,
        PHONES,
        CABLES_CHARGERS,
        AUDIO,
        ACCESSORIES,
        STORAGE,
        TECH_SERVICE,
        OTHERS
    )

    fun getIconForCategory(categoria: String): ImageVector {
        return when (categoria) {
            PHONES -> Icons.Default.Smartphone
            CABLES_CHARGERS -> Icons.Default.ElectricBolt
            AUDIO -> Icons.Default.Headphones
            ACCESSORIES -> Icons.Default.PhoneAndroid
            STORAGE -> Icons.Default.SdStorage
            TECH_SERVICE -> Icons.Default.Build
            ALL -> Icons.Default.Apps
            else -> Icons.Default.Widgets
        }
    }
}

