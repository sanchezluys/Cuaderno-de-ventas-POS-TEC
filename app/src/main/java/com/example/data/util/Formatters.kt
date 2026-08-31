package com.example.data.util

import com.example.data.model.PerfilLocal
import com.example.data.model.VentaConItems
import com.example.data.model.VentaEstado
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {

    val LOCALE_ES_CO: Locale = Locale.forLanguageTag("es-CO")

    // Default: Colombian Pesos (COP), thousand separator "."
    fun formatMoney(
        amount: Double,
        separadorMilesActivo: Boolean = true,
        separadorMilesCaracter: String = "."
    ): String {
        val isInteger = (amount % 1.0 == 0.0)

        val symbols = DecimalFormatSymbols(LOCALE_ES_CO).apply {
            if (separadorMilesCaracter == ",") {
                groupingSeparator = ','
                decimalSeparator = '.'
            } else {
                groupingSeparator = '.'
                decimalSeparator = ','
            }
        }

        val pattern = when {
            !separadorMilesActivo -> if (isInteger) "$ 0" else "$ 0.##"
            else -> if (isInteger) "$ #,##0" else "$ #,##0.##"
        }

        val df = DecimalFormat(pattern, symbols)
        return df.format(amount)
    }

    fun formatMoney(amount: Double, perfil: PerfilLocal?): String {
        val activo = perfil?.separadorMilesActivo ?: true
        val caracter = perfil?.separadorMilesCaracter ?: "."
        return formatMoney(amount, activo, caracter)
    }

    private val timeFormat = SimpleDateFormat("h:mm a", LOCALE_ES_CO)
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", LOCALE_ES_CO)
    private val dateFullFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", LOCALE_ES_CO)
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy h:mm a", LOCALE_ES_CO)

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp)).lowercase(LOCALE_ES_CO)
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatDateFull(timestamp: Long): String {
        val formatted = dateFullFormat.format(Date(timestamp))
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(LOCALE_ES_CO) else it.toString() }
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    fun getDayStartAndEnd(timestamp: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return Pair(startOfDay, endOfDay)
    }

    /**
     * Generates a digital receipt text for sharing
     */
    fun generateReceiptText(
        perfil: PerfilLocal?,
        ventaConItems: VentaConItems
    ): String {
        val tiendaNombre = perfil?.tiendaNombre?.ifBlank { "Tienda de Tecnología" } ?: "Tienda de Tecnología"
        val direccion = perfil?.direccion?.takeIf { it.isNotBlank() }
        val telefono = perfil?.telefono?.takeIf { it.isNotBlank() }
        val vendedor = ventaConItems.venta.vendedorNombre.ifBlank { perfil?.vendedorNombre ?: "Vendedor" }
        val fechaStr = formatDateTime(ventaConItems.venta.fechaHora)

        val sb = StringBuilder()
        sb.appendLine("================================")
        sb.appendLine(tiendaNombre.uppercase(LOCALE_ES_CO))
        if (direccion != null) sb.appendLine("Dir: $direccion")
        if (telefono != null) sb.appendLine("Tel: $telefono")
        sb.appendLine("================================")
        sb.appendLine("Ticket de Venta #${ventaConItems.venta.id}")
        sb.appendLine("Fecha: $fechaStr")
        sb.appendLine("Atendido por: $vendedor")
        if (!ventaConItems.venta.clienteNombre.isNullOrBlank()) {
            val telStr = if (!ventaConItems.venta.clienteTelefono.isNullOrBlank()) " (${ventaConItems.venta.clienteTelefono})" else ""
            sb.appendLine("Cliente: ${ventaConItems.venta.clienteNombre}$telStr")
        }
        sb.appendLine("--------------------------------")
        sb.appendLine("CANT  DESCRIPCIÓN          TOTAL")
        sb.appendLine("--------------------------------")

        ventaConItems.items.forEach { item ->
            val subtotalStr = formatMoney(item.subtotal, perfil)
            val nombreCorto = if (item.productoNombre.length > 20) item.productoNombre.take(18) + ".." else item.productoNombre
            sb.appendLine("${item.cantidad}x  $nombreCorto")
            sb.appendLine("     Unit: ${formatMoney(item.precioUnitario, perfil)} -> $subtotalStr")
        }

        sb.appendLine("--------------------------------")
        sb.appendLine("TOTAL: ${formatMoney(ventaConItems.venta.total, perfil)}")

        if (ventaConItems.venta.pagaCon != null && ventaConItems.venta.pagaCon > 0) {
            sb.appendLine("PAGÓ CON: ${formatMoney(ventaConItems.venta.pagaCon, perfil)}")
            val vueltos = ventaConItems.venta.vueltos ?: (ventaConItems.venta.pagaCon - ventaConItems.venta.total).coerceAtLeast(0.0)
            sb.appendLine("VUELTOS / CAMBIO: ${formatMoney(vueltos, perfil)}")
        }

        sb.appendLine("================================")
        sb.appendLine("¡Gracias por su compra!")
        sb.appendLine("Conserve este recibo para soporte y garantía.")
        return sb.toString()
    }

    fun generateGlobalReportText(
        perfil: PerfilLocal?,
        dateTimestamp: Long,
        ventas: List<VentaConItems>
    ): String {
        val tienda = perfil?.tiendaNombre ?: "Tienda"
        val vendedor = perfil?.vendedorNombre ?: "Vendedor"
        val dateStr = formatDate(dateTimestamp)
        val activas = ventas.filter { it.venta.estado == VentaEstado.ACTIVA }
        val anuladas = ventas.filter { it.venta.estado == VentaEstado.ANULADA }

        val totalDia = activas.sumOf { it.venta.total }
        val ventasRealizadasCount = activas.size
        val anulacionesCount = anuladas.size

        val productQuantities = mutableMapOf<String, Int>()
        activas.forEach { ventaConItems ->
            ventaConItems.items.forEach { item ->
                productQuantities[item.productoNombre] = (productQuantities[item.productoNombre] ?: 0) + item.cantidad
            }
        }

        val topProducts = productQuantities.toList()
            .sortedByDescending { it.second }

        val sb = StringBuilder()
        sb.appendLine("Reporte de Ventas — $tienda")
        sb.appendLine("Vendedor: $vendedor — Fecha: $dateStr")
        if (!perfil?.direccion.isNullOrBlank()) sb.appendLine("Dirección: ${perfil?.direccion}")
        sb.appendLine("Total del día: ${formatMoney(totalDia, perfil)}")
        sb.appendLine("Ventas realizadas: $ventasRealizadasCount")
        sb.appendLine("Anulaciones del día: $anulacionesCount")
        sb.appendLine()
        sb.appendLine("Productos más vendidos:")
        if (topProducts.isEmpty()) {
            sb.appendLine("(Sin ventas activas registradas)")
        } else {
            topProducts.forEach { (nombre, cant) ->
                sb.appendLine("- $nombre: $cant ${if (cant == 1) "unidad" else "unidades"}")
            }
        }

        return sb.toString().trimEnd()
    }

    fun generateDetailedReportText(
        perfil: PerfilLocal?,
        dateTimestamp: Long,
        ventas: List<VentaConItems>
    ): String {
        val tienda = perfil?.tiendaNombre ?: "Tienda"
        val vendedor = perfil?.vendedorNombre ?: "Vendedor"
        val dateStr = formatDate(dateTimestamp)
        val chronList = ventas.sortedBy { it.venta.fechaHora }

        val sb = StringBuilder()
        sb.appendLine("Reporte Detallado de Ventas — $tienda")
        sb.appendLine("Vendedor: $vendedor — Fecha: $dateStr")
        sb.appendLine()

        if (chronList.isEmpty()) {
            sb.appendLine("(No hay transacciones registradas para este día)")
        } else {
            chronList.forEachIndexed { index, item ->
                val num = index + 1
                val horaVenta = formatTime(item.venta.fechaHora)
                val totalMoney = formatMoney(item.venta.total, perfil)

                if (item.venta.estado == VentaEstado.ANULADA) {
                    val horaAnulada = item.venta.anuladaEn?.let { formatTime(it) } ?: horaVenta
                    sb.appendLine("Venta #$num — $horaVenta — ANULADA (anulada a las $horaAnulada) — Total original: $totalMoney")
                } else {
                    sb.appendLine("Venta #$num — $horaVenta — Total: $totalMoney")
                    if (item.venta.pagaCon != null) {
                        sb.appendLine("  [Paga con: ${formatMoney(item.venta.pagaCon, perfil)} | Vueltos: ${formatMoney(item.venta.vueltos ?: 0.0, perfil)}]")
                    }
                }

                item.items.forEach { linea ->
                    if (linea.cantidad == 1) {
                        sb.appendLine("  - ${linea.productoNombre} x1 — ${formatMoney(linea.subtotal, perfil)}")
                    } else {
                        sb.appendLine("  - ${linea.productoNombre} x${linea.cantidad} — ${formatMoney(linea.precioUnitario, perfil)} c/u")
                    }
                }
                sb.appendLine()
            }
        }

        return sb.toString().trimEnd()
    }
}
