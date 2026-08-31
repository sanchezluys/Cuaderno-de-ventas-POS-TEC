package com.example.data.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.example.data.model.PerfilLocal
import com.example.data.model.VentaConItems
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object ReceiptImageGenerator {

    fun generateReceiptBitmap(
        context: Context,
        perfil: PerfilLocal?,
        ventaConItems: VentaConItems
    ): Bitmap {
        val width = 720
        val items = ventaConItems.items
        val logoBitmap = ImageStorageHelper.loadBitmapFromPath(perfil?.logoUri)
        val hasLogo = logoBitmap != null
        val hasCustomer = !ventaConItems.venta.clienteNombre.isNullOrBlank()

        val headerBannerHeight = if (hasLogo) 290f else 200f
        val baseHeight = (if (hasLogo) 640 else 560) + (if (hasCustomer) 45 else 0)
        val itemHeight = 65
        val totalHeight = baseHeight + (items.size * itemHeight) + if (ventaConItems.venta.pagaCon != null) 120 else 40

        val bitmap = createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor("#F1F5F9".toColorInt()) // Slate-100 container

        // Receipt Card
        val cardPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        val cardRect = RectF(24f, 24f, (width - 24).toFloat(), (totalHeight - 24).toFloat())
        canvas.drawRoundRect(cardRect, 24f, 24f, cardPaint)

        // Header Background Banner
        val headerPaint = Paint().apply {
            color = "#0F172A".toColorInt() // Slate-900 Tech Dark
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        val headerRect = RectF(24f, 24f, (width - 24).toFloat(), headerBannerHeight)
        canvas.drawRoundRect(headerRect, 24f, 24f, headerPaint)
        // Cover bottom round corners of header
        canvas.drawRect(24f, headerBannerHeight - 40f, (width - 24).toFloat(), headerBannerHeight, headerPaint)

        // Paint configurations
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val headerSubPaint = Paint().apply {
            color = "#94A3B8".toColorInt() // Slate-400
            textSize = 20f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val cyanAccentPaint = Paint().apply {
            color = "#38BDF8".toColorInt() // Sky-400
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = "#1E293B".toColorInt()
            textSize = 22f
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = "#0F172A".toColorInt()
            textSize = 23f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val rightAlignPaint = Paint().apply {
            color = "#0F172A".toColorInt()
            textSize = 23f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        val dashPaint = Paint().apply {
            color = "#CBD5E1".toColorInt()
            strokeWidth = 3f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(12f, 8f), 0f)
            isAntiAlias = true
        }

        // Draw Logo if available
        if (logoBitmap != null) {
            val maxLogoSize = 80
            val ratio = logoBitmap.width.toFloat() / logoBitmap.height.toFloat()
            val logoW = if (ratio >= 1) maxLogoSize else (maxLogoSize * ratio).toInt().coerceAtLeast(1)
            val logoH = if (ratio <= 1) maxLogoSize else (maxLogoSize / ratio).toInt().coerceAtLeast(1)
            val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoW, logoH, true)
            val logoLeft = ((width - logoW) / 2).toFloat()
            val logoTop = 40f

            // White rounded backing badge for logo
            val logoBackingPaint = Paint().apply {
                color = Color.WHITE
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            val logoBadgeRect = RectF(logoLeft - 8f, logoTop - 8f, logoLeft + logoW + 8f, logoTop + logoH + 8f)
            canvas.drawRoundRect(logoBadgeRect, 14f, 14f, logoBackingPaint)
            canvas.drawBitmap(scaledLogo, logoLeft, logoTop, null)
        }

        // Draw Header Content
        val storeName = (perfil?.tiendaNombre?.ifBlank { "TIENDA DE TECNOLOGÍA" } ?: "TIENDA DE TECNOLOGÍA").uppercase(Formatters.LOCALE_ES_CO)
        val titleY = if (hasLogo) 165f else 80f
        val addressY = if (hasLogo) 205f else 120f
        val contactY = if (hasLogo) 240f else 155f

        canvas.drawText(storeName, (width / 2).toFloat(), titleY, titlePaint)

        val address = perfil?.direccion?.takeIf { it.isNotBlank() } ?: "Local Comercial"
        canvas.drawText(address, (width / 2).toFloat(), addressY, headerSubPaint)

        val phone = perfil?.telefono?.takeIf { it.isNotBlank() }?.let { "Tel: $it • " } ?: ""
        val seller = perfil?.vendedorNombre ?: ventaConItems.venta.vendedorNombre
        canvas.drawText("${phone}Atendido por: $seller", (width / 2).toFloat(), contactY, cyanAccentPaint)

        var y = if (hasLogo) 330f else 240f

        // Ticket ID and Date
        val ticketNo = "TICKET #%04d".format(ventaConItems.venta.id)
        canvas.drawText(ticketNo, 60f, y, boldTextPaint)
        val dateStr = Formatters.formatDateTime(ventaConItems.venta.fechaHora)
        canvas.drawText(dateStr, (width - 60).toFloat(), y, rightAlignPaint.apply { typeface = Typeface.DEFAULT; textSize = 20f })

        if (hasCustomer) {
            y += 30f
            val clientText = "Cliente: ${ventaConItems.venta.clienteNombre}" + if (!ventaConItems.venta.clienteTelefono.isNullOrBlank()) " • Tel: ${ventaConItems.venta.clienteTelefono}" else ""
            val clientPaint = Paint().apply {
                color = "#0369A1".toColorInt() // Sky-700
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(clientText, 60f, y, clientPaint)
        }

        y += 30f
        canvas.drawLine(60f, y, (width - 60).toFloat(), y, dashPaint)
        y += 35f

        // Table Header
        val colHeaderPaint = Paint().apply {
            color = "#64748B".toColorInt()
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("CANT", 60f, y, colHeaderPaint)
        canvas.drawText("PRODUCTO", 150f, y, colHeaderPaint)
        val colHeaderRight = Paint(colHeaderPaint).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText("TOTAL", (width - 60).toFloat(), y, colHeaderRight)

        y += 20f
        canvas.drawLine(60f, y, (width - 60).toFloat(), y, dashPaint)
        y += 35f

        // Items
        items.forEach { item ->
            val cantText = "${item.cantidad}x"
            canvas.drawText(cantText, 60f, y, boldTextPaint)

            // Shorten name if too long
            val maxLen = 22
            val pName = if (item.productoNombre.length > maxLen) item.productoNombre.take(maxLen - 2) + ".." else item.productoNombre
            canvas.drawText(pName, 150f, y, textPaint)

            val subtotalStr = Formatters.formatMoney(item.subtotal, perfil)
            canvas.drawText(subtotalStr, (width - 60).toFloat(), y, rightAlignPaint)

            y += 28f
            val unitStr = "c/u: ${Formatters.formatMoney(item.precioUnitario, perfil)}"
            canvas.drawText(unitStr, 150f, y, headerSubPaint.apply { textAlign = Paint.Align.LEFT; textSize = 18f })

            y += 38f
        }

        y += 10f
        canvas.drawLine(60f, y, (width - 60).toFloat(), y, dashPaint)
        y += 45f

        // Total
        val grandTotalPaint = Paint().apply {
            color = "#0F172A".toColorInt()
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val grandTotalRight = Paint().apply {
            color = "#0284C7".toColorInt() // Brand Primary
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("TOTAL:", 60f, y, grandTotalPaint)
        canvas.drawText(Formatters.formatMoney(ventaConItems.venta.total, perfil), (width - 60).toFloat(), y, grandTotalRight)

        // Paga Con & Vueltos
        if (ventaConItems.venta.pagaCon != null && ventaConItems.venta.pagaCon > 0) {
            y += 45f
            val subMoneyPaint = Paint().apply {
                color = "#475569".toColorInt()
                textSize = 22f
                isAntiAlias = true
            }
            val subMoneyRight = Paint().apply {
                color = "#1E293B".toColorInt()
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText("PAGÓ CON:", 60f, y, subMoneyPaint)
            canvas.drawText(Formatters.formatMoney(ventaConItems.venta.pagaCon, perfil), (width - 60).toFloat(), y, subMoneyRight)

            y += 40f
            val vueltos = ventaConItems.venta.vueltos ?: (ventaConItems.venta.pagaCon - ventaConItems.venta.total).coerceAtLeast(0.0)

            // Green box for Vueltos
            val changeBoxRect = RectF(60f, y - 28f, (width - 60).toFloat(), y + 18f)
            val changeBgPaint = Paint().apply {
                color = "#DCFCE7".toColorInt() // Green-100
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawRoundRect(changeBoxRect, 10f, 10f, changeBgPaint)

            val changeTextPaint = Paint().apply {
                color = "#166534".toColorInt() // Green-800
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val changeRightPaint = Paint().apply {
                color = "#166534".toColorInt() // Green-800
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText("VUELTOS / CAMBIO:", 76f, y, changeTextPaint)
            canvas.drawText(Formatters.formatMoney(vueltos, perfil), (width - 76).toFloat(), y, changeRightPaint)
        }

        y += 50f
        canvas.drawLine(60f, y, (width - 60).toFloat(), y, dashPaint)
        y += 40f

        // Footer
        val footerPaint = Paint().apply {
            color = "#64748B".toColorInt()
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val footerSmall = Paint().apply {
            color = "#94A3B8".toColorInt()
            textSize = 16f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("¡GRACIAS POR SU COMPRA!", (width / 2).toFloat(), y, footerPaint)
        y += 26f
        canvas.drawText("Comprobante de venta y soporte de garantía", (width / 2).toFloat(), y, footerSmall)

        return bitmap
    }

    fun saveReceiptImageToFile(context: Context, bitmap: Bitmap, ventaId: Int): File {
        val cacheFolder = File(context.cacheDir, "receipt_images")
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs()
        }
        val file = File(cacheFolder, "ticket_venta_$ventaId.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    fun shareReceiptImage(
        context: Context,
        perfil: PerfilLocal?,
        ventaConItems: VentaConItems
    ) {
        val bitmap = generateReceiptBitmap(context, perfil, ventaConItems)
        val file = saveReceiptImageToFile(context, bitmap, ventaConItems.venta.id)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Recibo de compra #${ventaConItems.venta.id} - ${perfil?.tiendaNombre ?: "Tienda"}")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        val chooser = Intent.createChooser(intent, "Compartir ticket con el cliente...")
        context.startActivity(chooser)
    }
}
