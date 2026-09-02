package com.example.ui.screens.pos

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ItemVentaDraft
import com.example.data.model.PerfilLocal
import com.example.data.model.Producto
import com.example.data.model.ProductIcons
import com.example.data.model.TechCategories
import com.example.data.model.VentaConItems
import com.example.data.util.ContactHelper
import com.example.data.util.Formatters
import com.example.data.util.PickPhoneContactContract
import com.example.data.util.ReceiptImageGenerator
import com.example.ui.viewmodel.SalesViewModel

enum class ContactSelectionMode {
    NONE,
    PHONE_CONTACT,
    MANUAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: SalesViewModel,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val cartItems by viewModel.cartItems.collectAsState()
    val cartTotal by viewModel.cartTotal.collectAsState()
    val productos by viewModel.activeProductosList.collectAsState()
    val dynamicCategories by viewModel.categoriasList.collectAsState()
    val perfil by viewModel.perfilState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TechCategories.ALL) }

    val categoriesFilterList = remember(dynamicCategories) {
        listOf(TechCategories.ALL) + dynamicCategories
    }

    // Quick Manual / Free text product dialog
    var showManualProductDialog by remember { mutableStateOf(false) }

    // Ticket / Cart Bottom Sheet
    var showCartSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Last completed sale for success receipt modal
    var completedSaleConItems by remember { mutableStateOf<VentaConItems?>(null) }

    // Filter products based on search and category
    val filteredProducts = remember(productos, searchQuery, selectedCategory) {
        productos.filter { prod ->
            val matchSearch = searchQuery.isBlank() || prod.nombre.contains(searchQuery, ignoreCase = true)
            val matchCategory = selectedCategory == TechCategories.ALL || prod.categoria.equals(selectedCategory, ignoreCase = true)
            matchSearch && matchCategory
        }
    }

    // Map for fast check of items in cart to show active badges on POS buttons
    val cartQtyMap = remember(cartItems) {
        val map = mutableMapOf<String, Int>()
        cartItems.forEach { item ->
            val key = if (item.productoCatalogoId != null) "id_${item.productoCatalogoId}" else "name_${item.productoNombre.lowercase()}"
            map[key] = (map[key] ?: 0) + item.cantidad
        }
        map
    }

    val totalItemCount = cartItems.sumOf { it.cantidad }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (cartItems.isNotEmpty()) 84.dp else 0.dp)
        ) {
            // TOP BAR: Search & Fast Manual Product Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Search bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("pos_catalog_search"),
                            placeholder = {
                                Text(
                                    "Buscar producto o accesorio...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                        )

                        // Big Touch Button for Custom / Non-cataloged Product
                        Button(
                            onClick = { showManualProductDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("pos_mode_freetext")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Manual",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CATEGORIES HORIZONTAL FILTER BAR (Tech Categories + Custom)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categoriesFilterList) { category ->
                            val isSelected = selectedCategory == category
                            val categoryCount = if (category == TechCategories.ALL) {
                                productos.size
                            } else {
                                productos.count { it.categoria.equals(category, ignoreCase = true) }
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = category,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        if (categoryCount > 0) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isSelected)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "$categoryCount",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                        color = if (isSelected)
                                                            MaterialTheme.colorScheme.onPrimary
                                                        else
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = TechCategories.getIconForCategory(category),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }
                    }
                }
            }

            // POS TOUCH GRID (Large, friendly buttons like retail POS software)
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PointOfSale,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (productos.isEmpty()) "No hay productos cargados" else "No hay productos en esta categoría",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (productos.isEmpty())
                                "Usa el botón 'Manual' arriba o ve a la pestaña Catálogo para agregar artículos de tu tienda."
                            else
                                "Prueba seleccionando 'Todos' o ingresa un producto manual.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showManualProductDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ingresar Producto Manual")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { producto ->
                        val qtyInCart = cartQtyMap["id_${producto.id}"] ?: 0
                        PosProductButton(
                            producto = producto,
                            qtyInCart = qtyInCart,
                            perfil = perfil,
                            onTap = {
                                viewModel.addProductToCart(
                                    nombre = producto.nombre,
                                    precioUnitario = producto.precioReferencia,
                                    cantidad = 1,
                                    productoCatalogoId = producto.id
                                )
                            }
                        )
                    }
                }
            }
        }

        // STICKY BOTTOM POS TICKET / CHECKOUT BAR
        AnimatedVisibility(
            visible = cartItems.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCartSheet = true },
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Live items badge & Grand Total
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "$totalItemCount",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        Column {
                            Text(
                                text = "TOTAL DE VENTA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Formatters.formatMoney(cartTotal, perfil),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("pos_cart_total_text")
                            )
                        }
                    }

                    // Right: Fast Action Button (Ver Ticket / Cobrar)
                    Button(
                        onClick = { showCartSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("pos_confirm_sale_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "VER TICKET ($totalItemCount)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }

    // POS TICKET & CHECKOUT MODAL BOTTOM SHEET WITH CHANGE / VUELTOS CALCULATION
    if (showCartSheet) {
        CheckoutBottomSheet(
            cartItems = cartItems,
            cartTotal = cartTotal,
            perfil = perfil,
            totalItemCount = totalItemCount,
            onDismiss = { showCartSheet = false },
            onClearCart = {
                viewModel.clearCart()
                showCartSheet = false
            },
            onIncrement = { item -> viewModel.updateDraftQuantity(item.draftId, 1) },
            onDecrement = { item -> viewModel.updateDraftQuantity(item.draftId, -1) },
            onRemove = { item -> viewModel.removeDraftItem(item.draftId) },
            onConfirmSale = { pagaCon, vueltos, clienteNombre, clienteTelefono ->
                viewModel.confirmSale(
                    pagaCon = pagaCon,
                    vueltos = vueltos,
                    clienteNombre = clienteNombre,
                    clienteTelefono = clienteTelefono,
                    onSuccess = { ventaConItems ->
                        completedSaleConItems = ventaConItems
                        showCartSheet = false
                    }
                )
            }
        )
    }

    // QUICK MANUAL PRODUCT DIALOG
    if (showManualProductDialog) {
        ManualProductDialog(
            onDismiss = { showManualProductDialog = false },
            onAdd = { name, price, qty ->
                viewModel.addProductToCart(
                    nombre = name,
                    precioUnitario = price,
                    cantidad = qty,
                    productoCatalogoId = null
                )
                showManualProductDialog = false
            }
        )
    }

    // POS SALE RECEIPT & CONFIRMATION DIALOG WITH IMAGE / TEXT SHARING
    if (completedSaleConItems != null) {
        SaleSuccessReceiptDialog(
            ventaConItems = completedSaleConItems!!,
            perfil = perfil,
            onDismiss = { completedSaleConItems = null },
            onNavigateToHistory = {
                completedSaleConItems = null
                onNavigateToHistory()
            }
        )
    }
}

/**
 * Bottom Sheet for Checkout with Cash, Change (Vueltos) Calculation, and Customer Contact
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutBottomSheet(
    cartItems: List<ItemVentaDraft>,
    cartTotal: Double,
    perfil: PerfilLocal?,
    totalItemCount: Int,
    onDismiss: () -> Unit,
    onClearCart: () -> Unit,
    onIncrement: (ItemVentaDraft) -> Unit,
    onDecrement: (ItemVentaDraft) -> Unit,
    onRemove: (ItemVentaDraft) -> Unit,
    onConfirmSale: (pagaCon: Double?, vueltos: Double?, clienteNombre: String?, clienteTelefono: String?) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var cashReceivedInput by remember { mutableStateOf("") }

    // Customer / Contact state
    var contactMode by remember { mutableStateOf(ContactSelectionMode.NONE) }
    var clienteNombre by remember { mutableStateOf("") }
    var clienteTelefono by remember { mutableStateOf("") }

    val pickContactLauncher = rememberLauncherForActivityResult(
        contract = PickPhoneContactContract()
    ) { contactUri ->
        if (contactUri != null) {
            val contactInfo = ContactHelper.getContactDetails(context, contactUri)
            clienteNombre = contactInfo.name
            clienteTelefono = contactInfo.phone ?: ""
            contactMode = ContactSelectionMode.PHONE_CONTACT
        }
    }

    val cashReceivedAmount = remember(cashReceivedInput) {
        cashReceivedInput.trim().replace(".", "").replace(",", "").toDoubleOrNull()
    }

    val calculatedChange = remember(cashReceivedAmount, cartTotal) {
        if (cashReceivedAmount != null && cashReceivedAmount >= cartTotal) {
            cashReceivedAmount - cartTotal
        } else {
            null
        }
    }

    val missingAmount = remember(cashReceivedAmount, cartTotal) {
        if (cashReceivedAmount != null && cashReceivedAmount > 0 && cashReceivedAmount < cartTotal) {
            cartTotal - cashReceivedAmount
        } else {
            null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PointOfSale,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Ticket de Venta",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${perfil?.tiendaNombre ?: "Local"} • Atiende: ${perfil?.vendedorNombre ?: "Vendedor"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (cartItems.isNotEmpty()) {
                    TextButton(
                        onClick = onClearCart,
                        modifier = Modifier.testTag("pos_clear_cart_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpiar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Items list in cart
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cartItems.forEach { item ->
                    CartItemRow(
                        item = item,
                        perfil = perfil,
                        onIncrement = { onIncrement(item) },
                        onDecrement = { onDecrement(item) },
                        onRemove = { onRemove(item) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Total to Pay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total a Pagar ($totalItemCount items):",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = Formatters.formatMoney(cartTotal, perfil),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CÁLCULO DE VUELTOS / EFECTIVO RECIBIDO
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CÁLCULO DE VUELTOS (EFECTIVO)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cashReceivedInput,
                        onValueChange = { cashReceivedInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pos_cash_received_input"),
                        label = { Text("Efectivo recibido / Paga con") },
                        placeholder = { Text("Ej: 50000") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (cashReceivedInput.isNotEmpty()) {
                                IconButton(onClick = { cashReceivedInput = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Borrar")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Cash Shortcuts (Colombian bill denominations)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { cashReceivedInput = cartTotal.toLong().toString() },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Exacto", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }

                        listOf(20000.0, 50000.0, 100000.0).forEach { billValue ->
                            if (billValue >= cartTotal || cartTotal < 100000.0) {
                                Button(
                                    onClick = { cashReceivedInput = billValue.toLong().toString() },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = "$${billValue.toLong() / 1000}k",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Vueltos / Missing Banner
                    if (calculatedChange != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFDCFCE7), // Green-100
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF15803D),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "VUELTOS A ENTREGAR:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF166534)
                                    )
                                }
                                Text(
                                    text = Formatters.formatMoney(calculatedChange, perfil),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF166534)
                                    )
                                )
                            }
                        }
                    } else if (missingAmount != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Dinero insuficiente. Faltan:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = Formatters.formatMoney(missingAmount, perfil),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CLIENTE / CONTACTO (OPCIONAL) SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CLIENTE / CONTACTO PARA EL TICKET",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3 Selector Modes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = contactMode == ContactSelectionMode.NONE,
                            onClick = {
                                contactMode = ContactSelectionMode.NONE
                                clienteNombre = ""
                                clienteTelefono = ""
                            },
                            label = { Text("Sin contacto", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(Icons.Default.PersonOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )

                        FilterChip(
                            selected = contactMode == ContactSelectionMode.PHONE_CONTACT,
                            onClick = {
                                contactMode = ContactSelectionMode.PHONE_CONTACT
                                pickContactLauncher.launch(null)
                            },
                            label = { Text("Del Teléfono", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(8.dp)
                        )

                        FilterChip(
                            selected = contactMode == ContactSelectionMode.MANUAL,
                            onClick = {
                                contactMode = ContactSelectionMode.MANUAL
                            },
                            label = { Text("Nuevo", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.weight(0.9f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when (contactMode) {
                        ContactSelectionMode.NONE -> {
                            Text(
                                text = "El ticket se emitirá como Consumidor Final (anónimo).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ContactSelectionMode.PHONE_CONTACT -> {
                            if (clienteNombre.isNotBlank()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Column {
                                                Text(
                                                    text = clienteNombre,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (clienteTelefono.isNotBlank()) {
                                                    Text(
                                                        text = clienteTelefono,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            TextButton(
                                                onClick = { pickContactLauncher.launch(null) },
                                                contentPadding = PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Text("Cambiar", style = MaterialTheme.typography.labelSmall)
                                            }
                                            IconButton(
                                                onClick = {
                                                    clienteNombre = ""
                                                    clienteTelefono = ""
                                                    contactMode = ContactSelectionMode.NONE
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Quitar",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { pickContactLauncher.launch(null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                ) {
                                    Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Seleccionar de Contactos del Teléfono")
                                }
                            }
                        }
                        ContactSelectionMode.MANUAL -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = clienteNombre,
                                    onValueChange = { clienteNombre = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Nombre del Cliente") },
                                    placeholder = { Text("Ej: Andrea Gómez") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = clienteTelefono,
                                    onValueChange = { clienteTelefono = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Teléfono / WhatsApp (Opcional)") },
                                    placeholder = { Text("Ej: 300 123 4567") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Checkout Button
            Button(
                onClick = {
                    val cash = cashReceivedAmount ?: cartTotal
                    val change = if (cash >= cartTotal) cash - cartTotal else 0.0
                    val finalClienteNombre = if (contactMode != ContactSelectionMode.NONE && clienteNombre.isNotBlank()) clienteNombre.trim() else null
                    val finalClienteTelefono = if (contactMode != ContactSelectionMode.NONE && clienteTelefono.isNotBlank()) clienteTelefono.trim() else null
                    onConfirmSale(cash, change, finalClienteNombre, finalClienteTelefono)
                },
                enabled = cartItems.isNotEmpty() && (missingAmount == null),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("pos_checkout_action_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REGISTRAR VENTA Y COBRAR",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Large, tactile POS Product Button (Touch Tile)
 */
@Composable
fun PosProductButton(
    producto: Producto,
    qtyInCart: Int,
    perfil: PerfilLocal?,
    onTap: () -> Unit
) {
    val isInCart = qtyInCart > 0
    val borderColor by animateColorAsState(
        targetValue = if (isInCart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
        label = "border"
    )
    val productIcon = ProductIcons.getIconForProduct(producto)

    Card(
        onClick = onTap,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInCart)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(if (isInCart) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isInCart) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(134.dp)
            .testTag("pos_product_tile_${producto.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Semi-transparent background illustration / watermark of product icon
            Icon(
                imageVector = productIcon,
                contentDescription = null,
                tint = if (isInCart)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                modifier = Modifier
                    .size(76.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 4.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Product Icon Badge + Category Chip & In-cart Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = productIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.height(22.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = producto.categoria,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (isInCart) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "+$qtyInCart",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                // Middle: Product Name
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Bottom: Large High-Contrast Price in COP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Formatters.formatMoney(producto.precioReferencia, perfil),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Line item row in POS Ticket
 */
@Composable
fun CartItemRow(
    item: ItemVentaDraft,
    perfil: PerfilLocal?,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productoNombre,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${Formatters.formatMoney(item.precioUnitario, perfil)} c/u",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quantity stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (item.cantidad == 1) Icons.Default.Delete else Icons.Default.Remove,
                        contentDescription = "Disminuir",
                        tint = if (item.cantidad == 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "${item.cantidad}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aumentar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Line Subtotal
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(84.dp)
            ) {
                Text(
                    text = Formatters.formatMoney(item.subtotal, perfil),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Subtotal",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Manual / Custom Non-Cataloged Product Entry Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualProductDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, price: Double, qty: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var qtyStr by remember { mutableStateOf("1") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun submit() {
        val trimmedName = name.trim()
        val parsedPrice = priceStr.trim().toDoubleOrNull()
        val parsedQty = qtyStr.trim().toIntOrNull() ?: 1

        if (trimmedName.isBlank()) {
            errorMessage = "Ingresa el nombre o servicio técnico"
            return
        }
        if (parsedPrice == null || parsedPrice < 0) {
            errorMessage = "Ingresa un precio válido (ej: 25000)"
            return
        }
        if (parsedQty <= 0) {
            errorMessage = "La cantidad debe ser al menos 1"
            return
        }

        onAdd(trimmedName, parsedPrice, parsedQty)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(text = "Producto / Servicio Rápido", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Agrega cualquier producto o servicio no registrado en el catálogo para esta venta:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pos_freetext_name"),
                    label = { Text("Nombre o Descripción") },
                    placeholder = { Text("Ej: Cable OTG, Reparación Display...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = {
                            priceStr = it
                            errorMessage = null
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("pos_freetext_price"),
                        label = { Text("Precio COP ($)") },
                        placeholder = { Text("15000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = {
                            qtyStr = it
                            errorMessage = null
                        },
                        modifier = Modifier
                            .weight(0.7f)
                            .testTag("pos_freetext_qty"),
                        label = { Text("Cant.") },
                        placeholder = { Text("1") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { submit() },
                modifier = Modifier.testTag("pos_freetext_add_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Agregar a Venta")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Sale Success Dialog with Ticket Details & Direct Image/Text Sharing
 */
@Composable
fun SaleSuccessReceiptDialog(
    ventaConItems: VentaConItems,
    perfil: PerfilLocal?,
    onDismiss: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val venta = ventaConItems.venta
    val items = ventaConItems.items
    val totalCount = items.sumOf { it.cantidad }

    fun shareAsImage() {
        ReceiptImageGenerator.shareReceiptImage(context, perfil, ventaConItems)
    }

    fun shareAsText() {
        val receiptText = Formatters.generateReceiptText(perfil, ventaConItems)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, receiptText)
            putExtra(Intent.EXTRA_TITLE, "Recibo de Venta #${venta.id}")
        }
        val chooser = Intent.createChooser(intent, "Compartir recibo vía...")
        context.startActivity(chooser)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "¡Venta #${venta.id} Exitosa!",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = perfil?.tiendaNombre ?: "Local Comercial",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Receipt Summary Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL COBRADO",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = Formatters.formatMoney(venta.total, perfil),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (venta.pagaCon != null && venta.pagaCon > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pagó con:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    Formatters.formatMoney(venta.pagaCon, perfil),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            val vueltos = venta.vueltos ?: (venta.pagaCon - venta.total).coerceAtLeast(0.0)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Vueltos:",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534)
                                    )
                                )
                                Text(
                                    Formatters.formatMoney(vueltos, perfil),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF166534)
                                    )
                                )
                            }
                        }

                        if (!venta.clienteNombre.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text("Cliente:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                }
                                Text(
                                    text = venta.clienteNombre + if (!venta.clienteTelefono.isNullOrBlank()) " (${venta.clienteTelefono})" else "",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$totalCount productos • ${Formatters.formatTime(venta.fechaHora)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action: Share Receipt Image to Customer
                Button(
                    onClick = { shareAsImage() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pos_share_ticket_image_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Compartir Ticket (Imagen)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action: Share Receipt as Text
                OutlinedButton(
                    onClick = { shareAsText() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("pos_share_ticket_text_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compartir Ticket (Texto)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("pos_sale_success_ok_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Nueva Venta")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onNavigateToHistory,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Ver Historial")
            }
        }
    )
}
