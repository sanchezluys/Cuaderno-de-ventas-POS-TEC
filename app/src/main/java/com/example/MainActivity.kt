package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.AppNavTab
import com.example.ui.components.StoreInfoDialog
import com.example.ui.components.TopAppBarWithProfile
import com.example.ui.screens.catalog.CatalogScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.pos.PosScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.setup.SetupScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SalesViewModel
import com.example.ui.viewmodel.SalesViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: SalesViewModel by viewModels {
        SalesViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: SalesViewModel) {
    val isProfileLoaded by viewModel.isProfileLoaded.collectAsState()
    val perfil by viewModel.perfilState.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by remember { mutableStateOf(AppNavTab.POS) }
    var showStoreInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUiMessage()
        }
    }

    if (!isProfileLoaded) {
        // Smooth initialization placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )
        }
    } else if (perfil == null) {
        // First time setup check: if perfil is null, show setup screen
        SetupScreen(
            onSaveProfile = { tienda, vendedor, direccion, telefono, sepActivo, sepChar, logoUri ->
                viewModel.savePerfil(
                    tiendaNombre = tienda,
                    vendedorNombre = vendedor,
                    direccion = direccion,
                    telefono = telefono,
                    separadorMilesActivo = sepActivo,
                    separadorMilesCaracter = sepChar,
                    logoUri = logoUri
                )
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBarWithProfile(
                    perfil = perfil,
                    onStoreInfoClick = { showStoreInfoDialog = true }
                )
            },
            bottomBar = {
                AppBottomNavigation(
                    selectedTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                    when (tab) {
                        AppNavTab.POS -> PosScreen(
                            viewModel = viewModel,
                            onNavigateToHistory = { currentTab = AppNavTab.HISTORY }
                        )
                        AppNavTab.HISTORY -> HistoryScreen(
                            viewModel = viewModel
                        )
                        AppNavTab.CATALOG -> CatalogScreen(
                            viewModel = viewModel
                        )
                        AppNavTab.REPORTS -> ReportsScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        if (showStoreInfoDialog) {
            StoreInfoDialog(
                perfil = perfil,
                onDismiss = { showStoreInfoDialog = false },
                onResetAllData = {
                    viewModel.resetAllAppData {
                        showStoreInfoDialog = false
                    }
                },
                onUpdateLogo = { newLogoUri ->
                    viewModel.updateLogo(newLogoUri)
                }
            )
        }
    }
}
