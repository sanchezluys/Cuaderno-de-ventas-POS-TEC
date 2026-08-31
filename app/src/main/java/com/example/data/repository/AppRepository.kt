package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.CategoriaCustom
import com.example.data.model.ItemVentaDraft
import com.example.data.model.PerfilLocal
import com.example.data.model.Producto
import com.example.data.model.TechCategories
import com.example.data.model.Venta
import com.example.data.model.VentaConItems
import com.example.data.model.VentaEstado
import com.example.data.model.VentaItem
import com.example.data.util.Formatters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AppRepository(private val database: AppDatabase) {

    private val productoDao = database.productoDao()
    private val ventaDao = database.ventaDao()
    private val ventaItemDao = database.ventaItemDao()
    private val perfilLocalDao = database.perfilLocalDao()
    private val categoriaDao = database.categoriaDao()

    companion object {
        const val MAX_CATALOGO_PRODUCTOS = 100
    }

    // --- Perfil Local ---
    val perfilFlow: Flow<PerfilLocal?> = perfilLocalDao.getPerfil()

    suspend fun getPerfil(): PerfilLocal? = perfilLocalDao.getPerfilSync()

    suspend fun savePerfil(
        tiendaNombre: String,
        vendedorNombre: String,
        direccion: String = "",
        telefono: String = "",
        separadorMilesActivo: Boolean = true,
        separadorMilesCaracter: String = ".",
        logoUri: String? = null
    ) {
        val cleanTienda = tiendaNombre.trim().ifBlank { "Tienda de Tecnología" }
        val cleanVendedor = vendedorNombre.trim().ifBlank { "Vendedor" }
        perfilLocalDao.savePerfil(
            PerfilLocal(
                id = 1,
                tiendaNombre = cleanTienda,
                vendedorNombre = cleanVendedor,
                direccion = direccion.trim(),
                telefono = telefono.trim(),
                separadorMilesActivo = separadorMilesActivo,
                separadorMilesCaracter = separadorMilesCaracter,
                logoUri = logoUri
            )
        )
    }

    suspend fun updateLogo(logoUri: String?) {
        val current = perfilLocalDao.getPerfilSync() ?: PerfilLocal()
        perfilLocalDao.savePerfil(current.copy(logoUri = logoUri))
    }

    suspend fun resetAllAppData() = withContext(Dispatchers.IO) {
        database.clearAllTables()
        // Re-seed default products for next shop setup
        seedDefaultProductsIfEmpty()
    }

    // --- Categorías Dinámicas ---
    val customCategoriasFlow: Flow<List<CategoriaCustom>> = categoriaDao.getAllCustomCategorias()

    val allCategoriasFlow: Flow<List<String>> = customCategoriasFlow.map { customs ->
        val defaultList = TechCategories.list
        val customNames = customs.map { it.nombre.trim() }
        val combined = LinkedHashSet<String>()
        combined.addAll(defaultList)
        combined.addAll(customNames)
        combined.toList()
    }

    suspend fun insertCategoria(nombre: String): Result<Long> {
        val clean = nombre.trim()
        if (clean.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre de la categoría no puede estar vacío."))
        }
        if (TechCategories.list.any { it.equals(clean, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("La categoría \"$clean\" ya existe."))
        }
        if (categoriaDao.existsCategoria(clean) > 0) {
            return Result.failure(IllegalArgumentException("La categoría \"$clean\" ya existe."))
        }
        val id = categoriaDao.insertCategoria(CategoriaCustom(nombre = clean))
        return Result.success(id)
    }

    // --- Catálogo de Productos ---
    val allProductos: Flow<List<Producto>> = productoDao.getAllProductos()
    val activeProductos: Flow<List<Producto>> = productoDao.getActiveProductos()
    val productosCount: Flow<Int> = productoDao.getProductosCount()

    suspend fun insertProducto(
        nombre: String,
        precio: Double,
        categoria: String = TechCategories.PHONES,
        icono: String = "",
        activo: Boolean = true
    ): Result<Long> {
        val currentCount = productoDao.getProductosCountSync()
        if (currentCount >= MAX_CATALOGO_PRODUCTOS) {
            return Result.failure(
                IllegalStateException("Límite del catálogo alcanzado ($MAX_CATALOGO_PRODUCTOS/$MAX_CATALOGO_PRODUCTOS). Esta versión permite registrar hasta $MAX_CATALOGO_PRODUCTOS productos en el catálogo. Puedes vender productos no catalogados usando la opción 'Manual' en Nueva Venta.")
            )
        }
        val cleanName = nombre.trim()
        val cleanCat = categoria.trim().ifBlank { TechCategories.PHONES }
        val cleanIcono = icono.trim()
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre del producto no puede estar vacío."))
        }
        if (precio < 0) {
            return Result.failure(IllegalArgumentException("El precio no puede ser negativo."))
        }
        val id = productoDao.insertProducto(
            Producto(
                nombre = cleanName,
                precioReferencia = precio,
                categoria = cleanCat,
                icono = cleanIcono,
                activo = activo
            )
        )
        return Result.success(id)
    }

    suspend fun updateProducto(producto: Producto): Result<Unit> {
        val cleanName = producto.nombre.trim()
        val cleanCat = producto.categoria.trim().ifBlank { TechCategories.PHONES }
        val cleanIcono = producto.icono.trim()
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre del producto no puede estar vacío."))
        }
        if (producto.precioReferencia < 0) {
            return Result.failure(IllegalArgumentException("El precio no puede ser negativo."))
        }
        productoDao.updateProducto(producto.copy(nombre = cleanName, categoria = cleanCat, icono = cleanIcono))
        return Result.success(Unit)
    }

    suspend fun setProductoActivo(productoId: Int, activo: Boolean) {
        productoDao.setActivo(productoId, activo)
    }

    suspend fun deleteProducto(producto: Producto) {
        productoDao.deleteProducto(producto)
    }

    suspend fun seedDefaultProductsIfEmpty() {
        val count = productoDao.getProductosCountSync()
        if (count == 0) {
            val sampleProducts = listOf(
                Producto(nombre = "Xiaomi Redmi Note 13 256GB 8GB RAM", precioReferencia = 780000.0, categoria = "Celulares", icono = "smartphone", activo = true),
                Producto(nombre = "Samsung Galaxy A15 128GB 4GB RAM", precioReferencia = 590000.0, categoria = "Celulares", icono = "smartphone", activo = true),
                Producto(nombre = "Motorola Moto G54 5G 256GB", precioReferencia = 680000.0, categoria = "Celulares", icono = "smartphone", activo = true),
                Producto(nombre = "Infinix Hot 40 Pro 256GB 8GB RAM", precioReferencia = 650000.0, categoria = "Celulares", icono = "smartphone", activo = true),

                Producto(nombre = "Cargador Rápido 65W GaN Tipo C", precioReferencia = 45000.0, categoria = "Cables & Carga", icono = "power", activo = true),
                Producto(nombre = "Cable Tipo C a Tipo C 100W 1.8m", precioReferencia = 22000.0, categoria = "Cables & Carga", icono = "usb", activo = true),
                Producto(nombre = "Cable Lightning Reforzado 1.2m", precioReferencia = 18000.0, categoria = "Cables & Carga", icono = "usb", activo = true),
                Producto(nombre = "Cargador Inalámbrico MagSafe 15W", precioReferencia = 55000.0, categoria = "Cables & Carga", icono = "electric_bolt", activo = true),
                
                Producto(nombre = "Audífonos Bluetooth TWS Pro", precioReferencia = 68000.0, categoria = "Audio & Sonido", icono = "headphones", activo = true),
                Producto(nombre = "Diadema Gamer RGB con Micrófono", precioReferencia = 85000.0, categoria = "Audio & Sonido", icono = "headphones", activo = true),
                Producto(nombre = "Parlante Bluetooth Portátil IPX6", precioReferencia = 75000.0, categoria = "Audio & Sonido", icono = "speaker", activo = true),

                Producto(nombre = "Vidrio Templado 9D Pantalla Completa", precioReferencia = 12000.0, categoria = "Accesorios & Fundas", icono = "shield", activo = true),
                Producto(nombre = "Funda Antigolpes con Anillo Soporte", precioReferencia = 25000.0, categoria = "Accesorios & Fundas", icono = "smartphone", activo = true),
                Producto(nombre = "Soporte Magnético Celular para Auto", precioReferencia = 20000.0, categoria = "Accesorios & Fundas", icono = "smartphone", activo = true),
                Producto(nombre = "Aro de Luz LED Trípode 26cm", precioReferencia = 38000.0, categoria = "Accesorios & Fundas", icono = "camera", activo = true),

                Producto(nombre = "Memoria MicroSD Kingston 128GB", precioReferencia = 38000.0, categoria = "Almacenamiento", icono = "sd_storage", activo = true),
                Producto(nombre = "Memoria USB 3.2 Metálica 64GB", precioReferencia = 24000.0, categoria = "Almacenamiento", icono = "sd_storage", activo = true),
                Producto(nombre = "Disco SSD Sólido Externo 500GB", precioReferencia = 180000.0, categoria = "Almacenamiento", icono = "sd_storage", activo = true),

                Producto(nombre = "Mantenimiento Preventivo / Limpieza", precioReferencia = 35000.0, categoria = "Servicio Técnico", icono = "build", activo = true),
                Producto(nombre = "Cambio de Pantalla / Módulo", precioReferencia = 120000.0, categoria = "Servicio Técnico", icono = "build", activo = true),
                Producto(nombre = "Formateo e Instalación de Software", precioReferencia = 40000.0, categoria = "Servicio Técnico", icono = "build", activo = true)
            )
            sampleProducts.forEach { productoDao.insertProducto(it) }
        }
    }

    // --- Ventas ---
    fun getVentasDelDia(dateTimestamp: Long = System.currentTimeMillis()): Flow<List<VentaConItems>> {
        val (start, end) = Formatters.getDayStartAndEnd(dateTimestamp)
        return ventaDao.getVentasDelDiaConItems(start, end)
    }

    fun getAllVentas(): Flow<List<VentaConItems>> = ventaDao.getAllVentasConItems()

    suspend fun registrarVenta(
        vendedorNombre: String,
        items: List<ItemVentaDraft>,
        pagaCon: Double? = null,
        vueltos: Double? = null,
        clienteNombre: String? = null,
        clienteTelefono: String? = null
    ): Result<Int> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Una venta debe tener al menos una línea de producto."))
        }

        // Business rule 3: Total is calculated strictly by summing item subtotals
        val totalCalculado = items.sumOf { it.precioUnitario * it.cantidad }
        val now = System.currentTimeMillis()

        val cleanClienteNombre = clienteNombre?.trim()?.takeIf { it.isNotBlank() }
        val cleanClienteTelefono = clienteTelefono?.trim()?.takeIf { it.isNotBlank() }

        val nuevaVenta = Venta(
            fechaHora = now,
            vendedorNombre = vendedorNombre.ifBlank { "Vendedor" },
            total = totalCalculado,
            estado = VentaEstado.ACTIVA,
            anuladaEn = null,
            pagaCon = pagaCon,
            vueltos = vueltos,
            clienteNombre = cleanClienteNombre,
            clienteTelefono = cleanClienteTelefono
        )

        val idLong = ventaDao.insertVenta(nuevaVenta)
        val vId = idLong.toInt()

        // Business rule: copy product name and price at this exact transaction moment (historical snapshot)
        val lineasVenta = items.map { draft ->
            VentaItem(
                ventaId = vId,
                productoNombre = draft.productoNombre.trim(),
                precioUnitario = draft.precioUnitario,
                cantidad = draft.cantidad,
                subtotal = draft.precioUnitario * draft.cantidad
            )
        }
        ventaItemDao.insertItems(lineasVenta)

        return Result.success(vId)
    }

    suspend fun getLastActiveVentaOfTheDay(dateTimestamp: Long = System.currentTimeMillis()): Venta? {
        val (start, end) = Formatters.getDayStartAndEnd(dateTimestamp)
        return ventaDao.getLastActiveVentaOfTheDay(start, end)
    }

    suspend fun anularUltimaVenta(ventaId: Int, dateTimestamp: Long = System.currentTimeMillis()): Result<Unit> {
        val (start, end) = Formatters.getDayStartAndEnd(dateTimestamp)
        val lastActive = ventaDao.getLastActiveVentaOfTheDay(start, end)

        if (lastActive == null || lastActive.id != ventaId) {
            return Result.failure(
                IllegalStateException("Solo se puede anular la última venta activa del día.")
            )
        }

        val rowsUpdated = ventaDao.anularVenta(ventaId, System.currentTimeMillis())
        return if (rowsUpdated > 0) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("No se pudo anular la venta."))
        }
    }
}

