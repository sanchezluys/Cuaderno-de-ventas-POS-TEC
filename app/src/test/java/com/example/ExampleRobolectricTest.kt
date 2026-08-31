package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Venta
import com.example.data.model.VentaConItems
import com.example.data.model.VentaEstado
import com.example.data.model.VentaItem
import com.example.data.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Cuaderno de Ventas", appName)
  }

  @Test
  fun `test global report excludes annulled sales from total`() {
    val now = System.currentTimeMillis()
    val ventaActiva = VentaConItems(
      venta = Venta(id = 1, fechaHora = now, vendedorNombre = "Carlos", total = 25000.0, estado = VentaEstado.ACTIVA),
      items = listOf(
        VentaItem(id = 1, ventaId = 1, productoNombre = "Cargador", precioUnitario = 25000.0, cantidad = 1, subtotal = 25000.0)
      )
    )
    val ventaAnulada = VentaConItems(
      venta = Venta(id = 2, fechaHora = now + 1000, vendedorNombre = "Carlos", total = 15000.0, estado = VentaEstado.ANULADA, anuladaEn = now + 2000),
      items = listOf(
        VentaItem(id = 2, ventaId = 2, productoNombre = "Cable USB", precioUnitario = 15000.0, cantidad = 1, subtotal = 15000.0)
      )
    )

    val perfil = com.example.data.model.PerfilLocal(
      tiendaNombre = "Tech Store",
      vendedorNombre = "Carlos"
    )
    val reportText = Formatters.generateGlobalReportText(perfil, now, listOf(ventaActiva, ventaAnulada))
    assertTrue(reportText.contains("Total del día: $ 25.000") || reportText.contains("25.000"))
    assertTrue(reportText.contains("Ventas realizadas: 1"))
    assertTrue(reportText.contains("Anulaciones del día: 1"))
  }
}

