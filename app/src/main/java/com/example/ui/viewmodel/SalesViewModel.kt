package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ItemVentaDraft
import com.example.data.model.PerfilLocal
import com.example.data.model.Producto
import com.example.data.model.Venta
import com.example.data.model.VentaConItems
import com.example.data.model.VentaEstado
import com.example.data.model.VentaItem
import com.example.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SalesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database)
        viewModelScope.launch {
            repository.seedDefaultProductsIfEmpty()
        }
    }

    // --- Profile Initial Load State ---
    val isProfileLoaded: StateFlow<Boolean> = repository.perfilFlow
        .map { true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    // --- Profile State ---
    val perfilState: StateFlow<PerfilLocal?> = repository.perfilFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    // --- Products Catalog State ---
    val productosList: StateFlow<List<Producto>> = repository.allProductos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeProductosList: StateFlow<List<Producto>> = repository.activeProductos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categoriasList: StateFlow<List<String>> = repository.allCategoriasFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val productosCount: StateFlow<Int> = repository.productosCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // --- POS Cart State ---
    private val _cartItems = MutableStateFlow<List<ItemVentaDraft>>(emptyList())
    val cartItems: StateFlow<List<ItemVentaDraft>> = _cartItems.asStateFlow()

    val cartTotal: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.precioUnitario * it.cantidad }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // --- Date for History and Reports ---
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    // --- Sales for Selected Date ---
    val ventasDelDia: StateFlow<List<VentaConItems>> = _selectedDate
        .flatMapLatest { date ->
            repository.getVentasDelDia(date)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- All Historical Sales for Performance Reports ---
    val allHistoricalSales: StateFlow<List<VentaConItems>> = repository.getAllVentas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ID of the single latest active sale of the selected day that can be annulled
    val lastActiveVentaId: StateFlow<Int?> = ventasDelDia.map { list ->
        list.firstOrNull { it.venta.estado == VentaEstado.ACTIVA }?.venta?.id
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // --- UI Feedback Messages ---
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // --- Last completed sale for receipt preview and instant sharing ---
    private val _lastCompletedSale = MutableStateFlow<VentaConItems?>(null)
    val lastCompletedSale: StateFlow<VentaConItems?> = _lastCompletedSale.asStateFlow()

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun dismissCompletedSale() {
        _lastCompletedSale.value = null
    }

    fun setDate(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    fun setToday() {
        _selectedDate.value = System.currentTimeMillis()
    }

    // --- Profile Operations ---
    fun savePerfil(
        tiendaNombre: String,
        vendedorNombre: String,
        direccion: String = "",
        telefono: String = "",
        separadorMilesActivo: Boolean = true,
        separadorMilesCaracter: String = ".",
        logoUri: String? = null
    ) {
        viewModelScope.launch {
            repository.savePerfil(
                tiendaNombre = tiendaNombre,
                vendedorNombre = vendedorNombre,
                direccion = direccion,
                telefono = telefono,
                separadorMilesActivo = separadorMilesActivo,
                separadorMilesCaracter = separadorMilesCaracter,
                logoUri = logoUri
            )
            _uiMessage.value = "¡Configuración de tienda guardada!"
        }
    }

    fun updateLogo(logoUri: String?) {
        viewModelScope.launch {
            repository.updateLogo(logoUri)
            _uiMessage.value = if (logoUri != null) "¡Logo del local actualizado!" else "Logo eliminado"
        }
    }

    // --- Destructive Full Reset ---
    fun resetAllAppData(onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            repository.resetAllAppData()
            _cartItems.value = emptyList()
            _lastCompletedSale.value = null
            _uiMessage.value = "Datos borrados. Puedes configurar una nueva tienda."
            onFinished()
        }
    }

    // --- Categorías Personalizadas ---
    fun addCategoria(nombre: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.insertCategoria(nombre)
            if (result.isSuccess) {
                _uiMessage.value = "Categoría \"${nombre.trim()}\" agregada"
                onResult(true, null)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Error al agregar categoría"
                _uiMessage.value = errorMsg
                onResult(false, errorMsg)
            }
        }
    }

    // --- Catalog Operations ---
    fun addProducto(
        nombre: String,
        precio: Double,
        categoria: String = "Celulares",
        icono: String = "",
        activo: Boolean = true,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.insertProducto(nombre, precio, categoria, icono, activo)
            if (result.isSuccess) {
                _uiMessage.value = "Producto \"$nombre\" agregado al catálogo"
                onResult(true, null)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Error al agregar producto"
                _uiMessage.value = errorMsg
                onResult(false, errorMsg)
            }
        }
    }

    fun updateProducto(producto: Producto, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateProducto(producto)
            if (result.isSuccess) {
                _uiMessage.value = "Producto actualizado"
                onResult(true, null)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Error al actualizar producto"
                _uiMessage.value = errorMsg
                onResult(false, errorMsg)
            }
        }
    }

    fun setProductoActivo(producto: Producto, activo: Boolean) {
        viewModelScope.launch {
            repository.setProductoActivo(producto.id, activo)
            _uiMessage.value = if (activo) "Producto \"${producto.nombre}\" activado para venta" else "Producto \"${producto.nombre}\" desactivado (no saldrá en ventas)"
        }
    }

    fun deleteProducto(producto: Producto) {
        viewModelScope.launch {
            repository.deleteProducto(producto)
            _uiMessage.value = "Producto \"${producto.nombre}\" eliminado"
        }
    }

    // --- POS Cart Operations ---
    fun addProductToCart(
        nombre: String,
        precioUnitario: Double,
        cantidad: Int = 1,
        productoCatalogoId: Int? = null
    ) {
        val cleanName = nombre.trim()
        if (cleanName.isBlank()) {
            _uiMessage.value = "Ingresa un nombre para el producto"
            return
        }
        if (precioUnitario < 0) {
            _uiMessage.value = "El precio unitario no puede ser negativo"
            return
        }
        if (cantidad <= 0) {
            _uiMessage.value = "La cantidad debe ser al menos 1"
            return
        }

        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst {
            if (productoCatalogoId != null && it.productoCatalogoId != null) {
                it.productoCatalogoId == productoCatalogoId && it.precioUnitario == precioUnitario
            } else {
                it.productoNombre.equals(cleanName, ignoreCase = true) && it.precioUnitario == precioUnitario
            }
        }

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            currentList[existingIndex] = existing.copy(cantidad = existing.cantidad + cantidad)
        } else {
            currentList.add(
                ItemVentaDraft(
                    productoCatalogoId = productoCatalogoId,
                    productoNombre = cleanName,
                    precioUnitario = precioUnitario,
                    cantidad = cantidad
                )
            )
        }
        _cartItems.value = currentList
    }

    fun updateDraftQuantity(draftId: String, delta: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.draftId == draftId }
        if (index >= 0) {
            val item = currentList[index]
            val newQty = item.cantidad + delta
            if (newQty > 0) {
                currentList[index] = item.copy(cantidad = newQty)
                _cartItems.value = currentList
            } else {
                currentList.removeAt(index)
                _cartItems.value = currentList
            }
        }
    }

    fun setDraftQuantity(draftId: String, newQty: Int) {
        if (newQty <= 0) {
            removeDraftItem(draftId)
            return
        }
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.draftId == draftId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(cantidad = newQty)
            _cartItems.value = currentList
        }
    }

    fun removeDraftItem(draftId: String) {
        val currentList = _cartItems.value.toMutableList()
        currentList.removeAll { it.draftId == draftId }
        _cartItems.value = currentList
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun confirmSale(
        pagaCon: Double? = null,
        vueltos: Double? = null,
        clienteNombre: String? = null,
        clienteTelefono: String? = null,
        onSuccess: (VentaConItems) -> Unit
    ) {
        val items = _cartItems.value
        if (items.isEmpty()) {
            _uiMessage.value = "Agrega al menos un producto a la venta antes de confirmar."
            return
        }

        val totalCalculado = items.sumOf { it.precioUnitario * it.cantidad }
        val now = System.currentTimeMillis()
        val sellerName = perfilState.value?.vendedorNombre ?: "Vendedor"

        val cleanClienteNombre = clienteNombre?.trim()?.takeIf { it.isNotBlank() }
        val cleanClienteTelefono = clienteTelefono?.trim()?.takeIf { it.isNotBlank() }

        viewModelScope.launch {
            val result = repository.registrarVenta(
                vendedorNombre = sellerName,
                items = items,
                pagaCon = pagaCon,
                vueltos = vueltos,
                clienteNombre = cleanClienteNombre,
                clienteTelefono = cleanClienteTelefono
            )
            if (result.isSuccess) {
                val ventaId = result.getOrThrow()
                val ventaObj = Venta(
                    id = ventaId,
                    fechaHora = now,
                    vendedorNombre = sellerName,
                    total = totalCalculado,
                    estado = VentaEstado.ACTIVA,
                    pagaCon = pagaCon,
                    vueltos = vueltos,
                    clienteNombre = cleanClienteNombre,
                    clienteTelefono = cleanClienteTelefono
                )
                val lineas = items.map {
                    VentaItem(
                        ventaId = ventaId,
                        productoNombre = it.productoNombre,
                        precioUnitario = it.precioUnitario,
                        cantidad = it.cantidad,
                        subtotal = it.precioUnitario * it.cantidad
                    )
                }
                val ventaConItems = VentaConItems(venta = ventaObj, items = lineas)
                _lastCompletedSale.value = ventaConItems
                _cartItems.value = emptyList()
                _uiMessage.value = "¡Venta #$ventaId registrada con éxito!"
                onSuccess(ventaConItems)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Error al registrar la venta"
                _uiMessage.value = errorMsg
            }
        }
    }

    // --- Annulment Operation ---
    fun anularUltimaVenta(ventaId: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.anularUltimaVenta(ventaId, _selectedDate.value)
            if (result.isSuccess) {
                _uiMessage.value = "Venta #$ventaId anulada correctamente."
                onResult(true, null)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "No se pudo anular la venta."
                _uiMessage.value = errorMsg
                onResult(false, errorMsg)
            }
        }
    }
}

class SalesViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SalesViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
