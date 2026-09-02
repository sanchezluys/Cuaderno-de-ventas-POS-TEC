package com.example

import com.example.data.model.CategoriaCustom
import com.example.data.model.PerfilLocal
import com.example.data.model.Producto
import com.example.data.model.TechCategories
import com.example.data.model.Venta
import com.example.data.model.VentaConItems
import com.example.data.model.VentaEstado
import com.example.data.model.VentaItem
import com.example.data.repository.AppRepository
import com.example.data.util.ContactInfo
import com.example.data.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCurrencyFormatting() {
        val formatted = Formatters.formatCurrency(50000.0)
        assertTrue(formatted.contains("50.000") || formatted.contains("50,000") || formatted.contains("50000"))
    }

    @Test
    fun testCatalogLimitConstant() {
        assertEquals(100, AppRepository.MAX_CATALOGO_PRODUCTOS)
    }

    @Test
    fun testTechCategoriesIncludesCelulares() {
        assertTrue(TechCategories.list.contains("Celulares"))
        assertTrue(TechCategories.filterList.contains("Celulares"))
    }

    @Test
    fun testCustomerContactOnReceipt() {
        val now = System.currentTimeMillis()
        val ventaConItems = VentaConItems(
            venta = Venta(
                id = 10,
                fechaHora = now,
                vendedorNombre = "Juan",
                total = 80000.0,
                clienteNombre = "Carlos Mendoza",
                clienteTelefono = "3001234567"
            ),
            items = listOf(
                VentaItem(
                    id = 1,
                    ventaId = 10,
                    productoNombre = "Funda Uso Rudo",
                    precioUnitario = 40000.0,
                    cantidad = 2,
                    subtotal = 80000.0
                )
            )
        )
        val perfil = PerfilLocal(
            tiendaNombre = "Mundo Móvil",
            vendedorNombre = "Juan"
        )
        val receiptText = Formatters.generateReceiptText(perfil, ventaConItems)
        assertTrue(receiptText.contains("Carlos Mendoza"))
        assertTrue(receiptText.contains("3001234567"))
        assertTrue(receiptText.contains("Mundo Móvil", ignoreCase = true))
        assertTrue(receiptText.contains("Funda Uso Rudo"))
    }

    @Test
    fun testProductActiveStateDefault() {
        val product = Producto(
            id = 1,
            nombre = "Cargador Carga Rápida 20W",
            precioReferencia = 35000.0,
            categoria = "Cargadores & Cables"
        )
        assertTrue(product.activo)
    }

    @Test
    fun testContactInfoDataClass() {
        val contact = ContactInfo(name = "María Pérez", phone = "+573105551234")
        assertEquals("María Pérez", contact.name)
        assertEquals("+573105551234", contact.phone)
    }
}

