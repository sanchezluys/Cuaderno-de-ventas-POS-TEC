package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.MobileFriendly
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.vector.ImageVector

data class ProductIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

object ProductIcons {
    val options = listOf(
        ProductIconOption("headphones", "Audífonos / Sonido", Icons.Default.Headphones),
        ProductIconOption("usb", "Cables / USB / Tipo C", Icons.Default.Usb),
        ProductIconOption("power", "Cargador / Adaptador", Icons.Default.Power),
        ProductIconOption("electric_bolt", "Carga Rápida / Powerbank", Icons.Default.ElectricBolt),
        ProductIconOption("smartphone", "Funda / Celular", Icons.Default.Smartphone),
        ProductIconOption("shield", "Vidrio / Protector / Mica", Icons.Default.Shield),
        ProductIconOption("sd_storage", "Memoria / Micro SD", Icons.Default.SdStorage),
        ProductIconOption("speaker", "Parlante / Bafle", Icons.AutoMirrored.Filled.VolumeUp),
        ProductIconOption("watch", "Smartwatch / Reloj", Icons.Default.Watch),
        ProductIconOption("build", "Servicio Técnico / Arreglo", Icons.Default.Build),
        ProductIconOption("mouse", "Mouse / Periférico", Icons.Default.Mouse),
        ProductIconOption("keyboard", "Teclado / Gamer", Icons.Default.Keyboard),
        ProductIconOption("gamepad", "Control / Videojuegos", Icons.Default.SportsEsports),
        ProductIconOption("camera", "Cámara / Aro de luz", Icons.Default.PhotoCamera),
        ProductIconOption("wifi", "Wifi / Router / Antena", Icons.Default.Wifi),
        ProductIconOption("battery", "Batería / Pila", Icons.Default.BatteryChargingFull),
        ProductIconOption("laptop", "Portátil / Computador", Icons.Default.Laptop),
        ProductIconOption("sim", "SIM / Chip / Recarga", Icons.Default.SimCard),
        ProductIconOption("bluetooth", "Bluetooth / Receptor", Icons.Default.Bluetooth),
        ProductIconOption("widgets", "Accesorio General", Icons.Default.Widgets)
    )

    private val optionsMap: Map<String, ImageVector> by lazy {
        options.associate { it.key to it.icon }
    }

    /**
     * Resolves the best icon for a product:
     * 1. Explicitly chosen icon key
     * 2. Smart automatic keyword recognition from product name
     * 3. Fallback category icon
     */
    fun getIconForProduct(producto: Producto): ImageVector {
        if (producto.icono.isNotBlank() && optionsMap.containsKey(producto.icono)) {
            return optionsMap[producto.icono]!!
        }

        val name = producto.nombre.lowercase()
        return when {
            name.containsAny("audifono", "auricular", "headphone", "earbud", "diadema", "in-ear", "manos libres", "tws", "earphones") ->
                Icons.Default.Headphones

            name.containsAny("cable", "tipo c", "type c", "lightning", "micro usb", "otg", "auxiliar", "hdmi", "cable usb") ->
                Icons.Default.Usb

            name.containsAny("cargador", "adaptador", "cubo", "muro", "fuente", "marroqui", "dock") ->
                Icons.Default.Power

            name.containsAny("power bank", "powerbank", "carga rapida", "bateria externa", "inversor") ->
                Icons.Default.ElectricBolt

            name.containsAny("funda", "estuche", "case", "forro", "bumper", "carcasa", "cover", "antichoque", "silicona") ->
                Icons.Default.Smartphone

            name.containsAny("vidrio", "cristal", "mica", "protector", "hidrogel", "templado", "screen protector", "cerámica") ->
                Icons.Default.Shield

            name.containsAny("memoria", "micro sd", "microsd", "sd", "pendrive", "usb drive", "disco duro", "ssd", "kingston", "sandisk") ->
                Icons.Default.SdStorage

            name.containsAny("parlante", "bocina", "speaker", "bafle", "soundbar", "altavoz", "corneta", "jbl") ->
                Icons.AutoMirrored.Filled.VolumeUp

            name.containsAny("reloj", "smartwatch", "band", "pulsera", "smart band", "watch") ->
                Icons.Default.Watch

            name.containsAny("servicio", "tecnico", "reparacion", "mantenimiento", "cambio", "display", "pantalla", "pin de carga", "revision", "instalacion") ->
                Icons.Default.Build

            name.containsAny("mouse", "raton", "mousepad", "optico") ->
                Icons.Default.Mouse

            name.containsAny("teclado", "keyboard", "mecanico") ->
                Icons.Default.Keyboard

            name.containsAny("control", "mando", "gamepad", "joystick", "gamer", "playstation", "xbox", "nintendo") ->
                Icons.Default.SportsEsports

            name.containsAny("camara", "camera", "aro de luz", "tripode", "webcam", "lente", "selfie") ->
                Icons.Default.PhotoCamera

            name.containsAny("wifi", "router", "repetidor", "antena", "red", "modem") ->
                Icons.Default.Wifi

            name.containsAny("bateria", "pila", "acumulador") ->
                Icons.Default.BatteryChargingFull

            name.containsAny("portatil", "laptop", "computador", "pc", "notebook", "tablet", "ipad") ->
                Icons.Default.Laptop

            name.containsAny("sim", "chip", "paquete", "recarga", "tigo", "claro", "movistar", "wom") ->
                Icons.Default.SimCard

            name.containsAny("bluetooth", "receptor", "transmisor", "dongle") ->
                Icons.Default.Bluetooth

            else -> TechCategories.getIconForCategory(producto.categoria)
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it, ignoreCase = true) }
    }
}
