package com.example.ui.screens.setup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.util.Formatters
import com.example.data.util.ImageStorageHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onSaveProfile: (
        tiendaNombre: String,
        vendedorNombre: String,
        direccion: String,
        telefono: String,
        separadorMiles: Boolean,
        caracterMiles: String,
        logoUri: String?
    ) -> Unit
) {
    val context = LocalContext.current

    var tiendaNombreInput by remember { mutableStateOf("") }
    var vendedorNombreInput by remember { mutableStateOf("") }
    var direccionInput by remember { mutableStateOf("") }
    var telefonoInput by remember { mutableStateOf("") }
    var separadorMilesActivo by remember { mutableStateOf(true) }
    var separadorMilesCaracter by remember { mutableStateOf(".") } // "." o ","
    var selectedLogoPath by remember { mutableStateOf<String?>(null) }

    var tiendaError by remember { mutableStateOf(false) }
    var vendedorError by remember { mutableStateOf(false) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImageToInternalStorage(context, uri)
            if (savedPath != null) {
                selectedLogoPath = savedPath
            }
        }
    }

    fun submit() {
        val cleanTienda = tiendaNombreInput.trim()
        val cleanVendedor = vendedorNombreInput.trim()

        var hasError = false
        if (cleanTienda.isBlank()) {
            tiendaError = true
            hasError = true
        }
        if (cleanVendedor.isBlank()) {
            vendedorError = true
            hasError = true
        }

        if (!hasError) {
            onSaveProfile(
                cleanTienda,
                cleanVendedor,
                direccionInput.trim(),
                telefonoInput.trim(),
                separadorMilesActivo,
                separadorMilesCaracter,
                selectedLogoPath
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon and Title Header
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Configuración Inicial del Local",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Configura tu negocio de tecnología. Tus datos se imprimirán en los recibos e informes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // High-Contrast Non-editable warning badge
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_warning_badge"),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Aviso importante de seguridad:",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Por control contable, los datos del local quedarán registrados. Si necesitas cambiarlos después, podrás reiniciar la tienda.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Logo Upload Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_logo_section"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val logoFile = selectedLogoPath?.let { File(it) }
                        if (logoFile != null && logoFile.exists()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(logoFile)
                                    .crossfade(true)
                                    .size(200, 200)
                                    .build(),
                                contentDescription = "Logo de la empresa seleccionado",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Logo de la Empresa (Cargado)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Aparecerá en el encabezado de los tickets.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { logoPickerLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("setup_change_logo_btn")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Cambiar")
                                }
                                TextButton(
                                    onClick = {
                                        ImageStorageHelper.deleteLogo(context)
                                        selectedLogoPath = null
                                    },
                                    modifier = Modifier.testTag("setup_remove_logo_btn")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Quitar", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Logo de la Empresa (Opcional)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Foto o archivo de imagen para imprimir en tus recibos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { logoPickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("setup_select_logo_btn")
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Subir Foto / Archivo")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input: Nombre del Local
                OutlinedTextField(
                    value = tiendaNombreInput,
                    onValueChange = {
                        tiendaNombreInput = it
                        if (tiendaError && it.isNotBlank()) tiendaError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_tienda_input"),
                    label = { Text("Nombre del Local o Tienda *") },
                    placeholder = { Text("Ej: TecnoStore San José") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    isError = tiendaError,
                    supportingText = {
                        if (tiendaError) {
                            Text("Ingresa el nombre del local para continuar.")
                        } else {
                            Text("Aparecerá en el encabezado de los tickets.")
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Input: Nombre del Vendedor
                OutlinedTextField(
                    value = vendedorNombreInput,
                    onValueChange = {
                        vendedorNombreInput = it
                        if (vendedorError && it.isNotBlank()) vendedorError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_vendedor_input"),
                    label = { Text("Nombre del Vendedor *") },
                    placeholder = { Text("Ej: Carlos Gómez") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    isError = vendedorError,
                    supportingText = {
                        if (vendedorError) {
                            Text("Ingresa el nombre del vendedor.")
                        } else {
                            Text("Identifica al vendedor a cargo del cuaderno.")
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Input: Dirección del Local
                OutlinedTextField(
                    value = direccionInput,
                    onValueChange = { direccionInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_direccion_input"),
                    label = { Text("Dirección del Local Comercial") },
                    placeholder = { Text("Ej: Cra 15 # 45-20 Local 102, Bogotá") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    supportingText = {
                        Text("Opcional. Se mostrará en los recibos.")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Input: Teléfono / WhatsApp
                OutlinedTextField(
                    value = telefonoInput,
                    onValueChange = { telefonoInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_telefono_input"),
                    label = { Text("Teléfono / WhatsApp de Contacto") },
                    placeholder = { Text("Ej: 310 123 4567") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    supportingText = {
                        Text("Opcional. Para contacto en recibos compartidos.")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Currency Configuration (Pesos Colombianos COP)
                Text(
                    text = "Configuración de Moneda (COP)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Switch for Thousands Separator
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Separador de miles",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (separadorMilesActivo) "Activo (ej: $ 150.000)" else "Inactivo (ej: $ 150000)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = separadorMilesActivo,
                            onCheckedChange = { separadorMilesActivo = it },
                            modifier = Modifier.testTag("setup_separador_switch")
                        )
                    }
                }

                if (separadorMilesActivo) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Símbolo de miles:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FilterChip(
                            selected = separadorMilesCaracter == ".",
                            onClick = { separadorMilesCaracter = "." },
                            label = { Text("Punto ( . )") },
                            modifier = Modifier.testTag("setup_chip_dot"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )

                        FilterChip(
                            selected = separadorMilesCaracter == ",",
                            onClick = { separadorMilesCaracter = "," },
                            label = { Text("Coma ( , )") },
                            modifier = Modifier.testTag("setup_chip_comma"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Live Preview of Price Formatting
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Vista previa:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = Formatters.formatMoney(150000.0, separadorMilesActivo, separadorMilesCaracter),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = { submit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("setup_submit_button"),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardar y Empezar a Vender",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "100% Offline • Sin servidores • En tu teléfono",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "v1.0.0 (Build 1) • 24 Ago 2026 • sanchezluys@gmail.com",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
}

