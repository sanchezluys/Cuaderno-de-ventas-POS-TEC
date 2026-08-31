package com.example.ui.screens.reports

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PerfilLocal
import com.example.data.model.VentaConItems
import com.example.data.model.VentaEstado
import com.example.data.util.Formatters
import com.example.ui.viewmodel.SalesViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DailyReportType {
    GLOBAL,
    DETALLADO
}

data class DayPerformanceData(
    val dateKey: String,
    val displayDate: String,
    val timestamp: Long,
    val totalRevenue: Double,
    val activeSalesCount: Int,
    val totalUnitsSold: Int,
    val averageTicket: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: SalesViewModel
) {
    val context = LocalContext.current
    val perfil by viewModel.perfilState.collectAsState()
    val ventasDelDia by viewModel.ventasDelDia.collectAsState()
    val allHistoricalSales by viewModel.allHistoricalSales.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var mainTabSelected by remember { mutableIntStateOf(0) } // 0: Reporte Diario, 1: Desempeño en el Tiempo
    var dailyReportType by remember { mutableStateOf(DailyReportType.GLOBAL) }

    fun shiftDay(days: Int) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedDate
        cal.add(Calendar.DAY_OF_YEAR, days)
        viewModel.setDate(cal.timeInMillis)
    }

    val isToday = remember(selectedDate) {
        val calSelected = Calendar.getInstance().apply { timeInMillis = selectedDate }
        val calNow = Calendar.getInstance()
        calSelected.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                calSelected.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)
    }

    val sellerName = perfil?.vendedorNombre ?: "Vendedor"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main Navigation Tabs (Diario vs Desempeño Histórico)
        TabRow(
            selectedTabIndex = mainTabSelected,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = mainTabSelected == 0,
                onClick = { mainTabSelected = 0 },
                text = { Text("Reporte Diario", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("reports_tab_daily")
            )
            Tab(
                selected = mainTabSelected == 1,
                onClick = { mainTabSelected = 1 },
                text = { Text("Desempeño en el Tiempo", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("reports_tab_performance")
            )
        }

        if (mainTabSelected == 0) {
            // --- TAB 1: REPORTE DIARIO (GLOBAL / DETALLADO) ---
            DailyReportView(
                context = context,
                sellerName = sellerName,
                perfil = perfil,
                selectedDate = selectedDate,
                isToday = isToday,
                ventas = ventasDelDia,
                reportType = dailyReportType,
                onReportTypeChange = { dailyReportType = it },
                onShiftDay = { shiftDay(it) },
                onGoToToday = { viewModel.setToday() }
            )
        } else {
            // --- TAB 2: HISTÓRICO Y EVOLUCIÓN DEL VENDEDOR EN EL TIEMPO ---
            SellerPerformanceHistoricalView(
                context = context,
                perfil = perfil,
                allSales = allHistoricalSales
            )
        }
    }
}

/**
 * View 1: Daily Report View with format preview and share/copy options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReportView(
    context: Context,
    sellerName: String,
    perfil: PerfilLocal?,
    selectedDate: Long,
    isToday: Boolean,
    ventas: List<VentaConItems>,
    reportType: DailyReportType,
    onReportTypeChange: (DailyReportType) -> Unit,
    onShiftDay: (Int) -> Unit,
    onGoToToday: () -> Unit
) {
    val reportText = remember(reportType, sellerName, selectedDate, ventas, perfil) {
        when (reportType) {
            DailyReportType.GLOBAL -> Formatters.generateGlobalReportText(perfil, selectedDate, ventas)
            DailyReportType.DETALLADO -> Formatters.generateDetailedReportText(perfil, selectedDate, ventas)
        }
    }

    fun shareReport() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, reportText)
            putExtra(Intent.EXTRA_TITLE, "Reporte de Ventas - $sellerName")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartir reporte vía...")
        context.startActivity(shareIntent)
    }

    fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Reporte de Ventas", reportText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Reporte copiado al portapapeles", Toast.LENGTH_SHORT).show()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Date switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onShiftDay(-1) },
                        modifier = Modifier.testTag("reports_prev_day_btn")
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Día anterior")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isToday) "Hoy (${Formatters.formatDate(selectedDate)})" else Formatters.formatDateFull(selectedDate),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Fecha del reporte",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        if (!isToday) {
                            IconButton(
                                onClick = onGoToToday,
                                modifier = Modifier.testTag("reports_today_btn")
                            ) {
                                Icon(Icons.Default.Today, contentDescription = "Ir a hoy", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        IconButton(
                            onClick = { onShiftDay(1) },
                            modifier = Modifier.testTag("reports_next_day_btn")
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Día siguiente")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Report Type Selector
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = reportType == DailyReportType.GLOBAL,
                        onClick = { onReportTypeChange(DailyReportType.GLOBAL) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = {
                            Icon(imageVector = Icons.Default.Summarize, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.testTag("reports_type_global")
                    ) {
                        Text(text = "Reporte Global", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }

                    SegmentedButton(
                        selected = reportType == DailyReportType.DETALLADO,
                        onClick = { onReportTypeChange(DailyReportType.DETALLADO) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.testTag("reports_type_detailed")
                    ) {
                        Text(text = "Reporte Detallado", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Preview & Actions Area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VISTA PREVIA DEL TEXTO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${ventas.size} transacciones",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Formatted Document Preview
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = reportText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("reports_preview_text")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons (Share & Copy)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { copyToClipboard() },
                    modifier = Modifier
                        .weight(0.8f)
                        .height(52.dp)
                        .testTag("reports_copy_btn"),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar")
                }

                Button(
                    onClick = { shareReport() },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(52.dp)
                        .testTag("reports_share_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compartir", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

/**
 * View 2: Historical Performance & Evolution Report (Seller Performance Over Time)
 */
@Composable
fun SellerPerformanceHistoricalView(
    context: Context,
    perfil: PerfilLocal?,
    allSales: List<VentaConItems>
) {
    val activeSales = remember(allSales) {
        allSales.filter { it.venta.estado == VentaEstado.ACTIVA }
    }

    // Group active sales by day (YYYY-MM-DD)
    val dayFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayFormat = remember { SimpleDateFormat("EEEE, d 'de' MMMM yyyy", Formatters.LOCALE_ES_CO) }

    val daysPerformanceList = remember(activeSales) {
        val groups = activeSales.groupBy { sale ->
            dayFormat.format(Date(sale.venta.fechaHora))
        }

        groups.map { (dateKey, salesInDay) ->
            val total = salesInDay.sumOf { it.venta.total }
            val count = salesInDay.size
            val units = salesInDay.sumOf { it.items.sumOf { item -> item.cantidad } }
            val firstTimestamp = salesInDay.first().venta.fechaHora
            val display = displayFormat.format(Date(firstTimestamp))
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Formatters.LOCALE_ES_CO) else it.toString() }

            DayPerformanceData(
                dateKey = dateKey,
                displayDate = display,
                timestamp = firstTimestamp,
                totalRevenue = total,
                activeSalesCount = count,
                totalUnitsSold = units,
                averageTicket = if (count > 0) total / count else 0.0
            )
        }.sortedByDescending { it.timestamp }
    }

    val totalDaysWithSales = daysPerformanceList.size
    val totalHistoricalRevenue = activeSales.sumOf { it.venta.total }
    val totalTicketsCount = activeSales.size
    val overallAverageTicket = if (totalTicketsCount > 0) totalHistoricalRevenue / totalTicketsCount else 0.0
    val dailyAverageRevenue = if (totalDaysWithSales > 0) totalHistoricalRevenue / totalDaysWithSales else 0.0
    val bestDay = daysPerformanceList.maxByOrNull { it.totalRevenue }

    // Performance comparison: Latest day vs historical daily average
    val latestDay = daysPerformanceList.firstOrNull()
    val performanceTrend = remember(latestDay, dailyAverageRevenue) {
        if (latestDay == null || totalDaysWithSales <= 1) {
            "📊 Recopilando datos históricos iniciales"
        } else if (latestDay.totalRevenue >= dailyAverageRevenue * 1.15) {
            "🚀 ¡Rendimiento en Alza! (+${((latestDay.totalRevenue / dailyAverageRevenue - 1) * 100).toInt()}% sobre el promedio)"
        } else if (latestDay.totalRevenue >= dailyAverageRevenue * 0.9) {
            "✅ Desempeño Estable y Consistente"
        } else {
            "📉 Por debajo del promedio diario (${Formatters.formatMoney(dailyAverageRevenue, perfil)})"
        }
    }

    fun shareHistoricalSummary() {
        val store = perfil?.tiendaNombre ?: "Local de Tecnología"
        val seller = perfil?.vendedorNombre ?: "Vendedor"
        val sb = StringBuilder()
        sb.append("📊 INFORME DE DESEMPEÑO HISTÓRICO DE VENTAS\n")
        sb.append("=========================================\n")
        sb.append("Tienda: $store\n")
        sb.append("Vendedor: $seller\n")
        sb.append("Fecha de emisión: ${Formatters.formatDate(System.currentTimeMillis())}\n\n")

        sb.append("📈 MÉTRICAS GLOBALES DE RENDIMIENTO:\n")
        sb.append("• Total Recaudado Acumulado: ${Formatters.formatMoney(totalHistoricalRevenue, perfil)}\n")
        sb.append("• Días trabajados con ventas: $totalDaysWithSales días\n")
        sb.append("• Total Tickets / Clientes atendidos: $totalTicketsCount\n")
        sb.append("• Promedio de Ventas por Día: ${Formatters.formatMoney(dailyAverageRevenue, perfil)}\n")
        sb.append("• Ticket Promedio por Venta: ${Formatters.formatMoney(overallAverageTicket, perfil)}\n")
        if (bestDay != null) {
            sb.append("• Mejor Día Registrado: ${bestDay.displayDate} (${Formatters.formatMoney(bestDay.totalRevenue, perfil)})\n")
        }
        sb.append("• Estado Actual: $performanceTrend\n\n")

        sb.append("📅 HISTORIAL DÍA POR DÍA:\n")
        sb.append("-----------------------------------------\n")
        daysPerformanceList.forEach { day ->
            sb.append("• ${day.displayDate}\n")
            sb.append("  Total: ${Formatters.formatMoney(day.totalRevenue, perfil)} | ${day.activeSalesCount} ventas | ${day.totalUnitsSold} unid.\n")
            sb.append("  Ticket Promedio: ${Formatters.formatMoney(day.averageTicket, perfil)}\n\n")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            putExtra(Intent.EXTRA_TITLE, "Desempeño Histórico - $seller")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Compartir informe de desempeño vía..."))
    }

    if (daysPerformanceList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoGraph,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "No hay histórico de ventas aún",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "A medida que registres ventas en diferentes días, aquí verás el análisis de evolución y desempeño del vendedor en el tiempo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Card: Global Performance Overview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Desempeño del Vendedor",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Evaluación de ventas en el tiempo • ${perfil?.vendedorNombre ?: "Vendedor"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoGraph,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Trend Badge
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = performanceTrend,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4 Key KPI Metrics Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KpiCard(
                                title = "Total Acumulado",
                                value = Formatters.formatMoney(totalHistoricalRevenue, perfil),
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                title = "Días con Ventas",
                                value = "$totalDaysWithSales días",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KpiCard(
                                title = "Promedio Diario",
                                value = Formatters.formatMoney(dailyAverageRevenue, perfil),
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                title = "Ticket Promedio",
                                value = Formatters.formatMoney(overallAverageTicket, perfil),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (bestDay != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEF3C7), // Amber 100
                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MilitaryTech,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Mejor Día Histórico:",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF92400E)
                                        )
                                        Text(
                                            text = "${bestDay.displayDate} • ${Formatters.formatMoney(bestDay.totalRevenue, perfil)}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF78350F)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Share Performance Report Button
                        Button(
                            onClick = { shareHistoricalSummary() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("share_performance_report_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Compartir Informe de Desempeño", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section Title: Day by day breakdown
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EVOLUCIÓN DÍA POR DÍA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$totalDaysWithSales fechas registradas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Day Cards
            val maxDayRevenue = bestDay?.totalRevenue?.coerceAtLeast(1.0) ?: 1.0
            items(daysPerformanceList, key = { it.dateKey }) { dayData ->
                DayPerformanceCard(
                    dayData = dayData,
                    maxDayRevenue = maxDayRevenue,
                    dailyAverage = dailyAverageRevenue,
                    perfil = perfil
                )
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DayPerformanceCard(
    dayData: DayPerformanceData,
    maxDayRevenue: Double,
    dailyAverage: Double,
    perfil: PerfilLocal?
) {
    val progress = (dayData.totalRevenue / maxDayRevenue).toFloat().coerceIn(0.05f, 1f)
    val isAboveAverage = dayData.totalRevenue >= dailyAverage

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dayData.displayDate,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${dayData.activeSalesCount} ventas cobradas • ${dayData.totalUnitsSold} unidades",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatMoney(dayData.totalRevenue, perfil),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Ticket prom: ${Formatters.formatMoney(dayData.averageTicket, perfil)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual bar of relative performance
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isAboveAverage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
