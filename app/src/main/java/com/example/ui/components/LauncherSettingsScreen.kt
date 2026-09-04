package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewSidebar
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.GestureAction
import com.example.model.HomeScreenStyle
import com.example.model.IconShape
import com.example.model.LauncherApp
import com.example.model.LauncherThemeStyle
import com.example.ui.LauncherUiState
import com.example.ui.LauncherViewModel
import com.example.ui.theme.WallpaperTheme
import com.example.ui.theme.WallpaperThemeEngine
import com.example.util.DefaultAppsResolver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherSettingsScreen(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    onRequestDefaultHome: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expandedSection by remember { mutableStateOf<String?>("appearance") }

    // Android Photo Picker for Custom Wallpaper
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setCustomWallpaperUri(uri.toString())
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0D1117),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Configurações do Front",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Personalização completa do launcher",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setEditMode(!uiState.isEditMode) }) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Modo de Edição",
                            tint = if (uiState.isEditMode) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF161B22)
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }

            // 1. APARÊNCIA & TEMAS
            item {
                SettingsCategorySection(
                    title = "Aparência & Estilos",
                    subtitle = "Temas, formatos e tamanho dos ícones",
                    icon = Icons.Rounded.Palette,
                    isExpanded = expandedSection == "appearance",
                    onToggle = {
                        expandedSection = if (expandedSection == "appearance") null else "appearance"
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Tema Visual
                        Text(
                            text = "Estilo de Tema",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LauncherThemeStyle.entries.forEach { style ->
                                val isSelected = uiState.themeStyle == style
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setLauncherThemeStyle(style) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = style.displayName,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Formato dos Ícones
                        Text(
                            text = "Formato dos Ícones",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconShape.entries.forEach { shape ->
                                val isSelected = uiState.iconShape == shape
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setIconShape(shape) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = shape.label,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Tamanho do Ícone Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tamanho dos Ícones: ${uiState.iconSizeDp}dp",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Slider(
                            value = uiState.iconSizeDp.toFloat(),
                            onValueChange = { viewModel.setIconSize(it.toInt()) },
                            valueRange = 44f..72f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        // Switches
                        SettingsSwitchRow(
                            title = "Ícones Temáticos (Monocromáticos)",
                            subtitle = "Adaptar os ícones à cor dinâmica do papel de parede",
                            checked = uiState.iconThemed,
                            onCheckedChange = { viewModel.setIconThemed(it) }
                        )

                        SettingsSwitchRow(
                            title = "Exibir Rótulos",
                            subtitle = "Mostrar os nomes dos aplicativos abaixo dos ícones",
                            checked = uiState.showLabels,
                            onCheckedChange = { viewModel.setShowLabels(it) }
                        )
                    }
                }
            }

            // 2. PAPÉIS DE PAREDE & FOTOS
            item {
                SettingsCategorySection(
                    title = "Papéis de Parede & Imagens",
                    subtitle = "Carregue suas próprias fotos ou use gradientes dinâmicos",
                    icon = Icons.Rounded.Wallpaper,
                    isExpanded = expandedSection == "wallpaper",
                    onToggle = {
                        expandedSection = if (expandedSection == "wallpaper") null else "wallpaper"
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Custom Photo Wallpaper Card
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.06f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Foto Personalizada",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (uiState.customWallpaperUri != null) "Foto da galeria ativa" else "Nenhuma foto selecionada",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Image,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Escolher Foto", fontSize = 12.sp)
                                    }
                                }

                                if (uiState.customWallpaperUri != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        ) {
                                            AsyncImage(
                                                model = uiState.customWallpaperUri,
                                                contentDescription = "Preview do Papel de Parede",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.setCustomWallpaperUri(null) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.DeleteOutline,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Remover Foto", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Presets de Wallpapers
                        Text(
                            text = "Coleção de Gradientes Dinâmicos",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(WallpaperThemeEngine.wallpaperList) { wp ->
                                val isSelected = uiState.currentWallpaper.id == wp.id && uiState.customWallpaperUri == null
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 2.dp else 0.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(110.dp)
                                        .clickable {
                                            viewModel.setCustomWallpaperUri(null)
                                            viewModel.setWallpaper(wp)
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(wp.backgroundBrush)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = wp.name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            modifier = Modifier.align(Alignment.BottomStart)
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .align(Alignment.TopEnd)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. TELA INICIAL & MULTI-PÁGINAS
            item {
                SettingsCategorySection(
                    title = "Tela Inicial & Multi-Páginas",
                    subtitle = "Arraste em tempo real, criar novas páginas e layout",
                    icon = Icons.Rounded.Home,
                    isExpanded = expandedSection == "home_screen",
                    onToggle = {
                        expandedSection = if (expandedSection == "home_screen") null else "home_screen"
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Modo de Edição em Tempo Real
                        SettingsSwitchRow(
                            title = "Modo de Edição em Tempo Real",
                            subtitle = "Exibir controles de arraste e remoção diretamente na tela inicial",
                            checked = uiState.isEditMode,
                            onCheckedChange = { viewModel.setEditMode(it) }
                        )

                        // Multi-Páginas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Páginas da Tela Inicial",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Atualmente: ${uiState.homePageCount} tela(s)",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.removeHomePage() },
                                    enabled = uiState.homePageCount > 1,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(imageVector = Icons.Rounded.Remove, contentDescription = "Remover Tela", modifier = Modifier.size(16.dp))
                                }
                                Button(
                                    onClick = { viewModel.addHomePage() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Nova Tela", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Nova Tela", fontSize = 12.sp)
                                }
                            }
                        }

                        // Colunas da Grade
                        Text(
                            text = "Colunas da Grade",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(4, 5).forEach { cols ->
                                val isSelected = uiState.gridColumns == cols
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setGridColumns(cols) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "$cols Colunas",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                                        )
                                    }
                                }
                            }
                        }

                        SettingsSwitchRow(
                            title = "Adicionar Novos Apps Automaticamente",
                            subtitle = "Colocar atalho na tela inicial ao instalar um novo aplicativo",
                            checked = uiState.addNewAppsToHome,
                            onCheckedChange = { viewModel.toggleAddNewAppsToHome() }
                        )
                    }
                }
            }

            // 4. DOCK DE APLICATIVOS (OBRIGATÓRIO ATÉ 5 APPS)
            item {
                SettingsCategorySection(
                    title = "Dock de Aplicativos",
                    subtitle = "Até 5 aplicativos essenciais com sugestão inteligente",
                    icon = Icons.Rounded.ViewSidebar,
                    isExpanded = expandedSection == "dock",
                    onToggle = {
                        expandedSection = if (expandedSection == "dock") null else "dock"
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selecionados: ${uiState.dockApps.size} de 5",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.dockApps.size in 1..5) MaterialTheme.colorScheme.primary else Color(0xFFEF4444)
                            )

                            OutlinedButton(
                                onClick = {
                                    val suggestedPkgs = DefaultAppsResolver.getDefaultAppsPackages(context)
                                    val suggestedApps = uiState.apps.filter { it.packageName in suggestedPkgs }.take(5)
                                    if (suggestedApps.isNotEmpty()) {
                                        viewModel.updateDockApps(suggestedApps)
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                            ) {
                                Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sugerir com IA", fontSize = 11.sp)
                            }
                        }

                        // Lista rápida de seleção do Dock
                        Text(
                            text = "Toque em um app para alternar no Dock (máx 5):",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )

                        val availableApps = uiState.apps.filter { !it.isHidden }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableApps) { app ->
                                val isInDock = uiState.dockApps.any { it.packageName == app.packageName }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isInDock) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isInDock) 1.5.dp else 0.5.dp,
                                        color = if (isInDock) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.clickable {
                                        if (isInDock) {
                                            viewModel.updateDockApps(uiState.dockApps.filter { it.packageName != app.packageName })
                                        } else if (uiState.dockApps.size < 5) {
                                            viewModel.updateDockApps(uiState.dockApps + app)
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isInDock) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = app.label,
                                            fontSize = 11.sp,
                                            color = if (isInDock) MaterialTheme.colorScheme.primary else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. GAVETA DE APLICATIVOS (CATEGORIAS ON/OFF)
            item {
                SettingsCategorySection(
                    title = "Gaveta de Aplicativos (App Drawer)",
                    subtitle = "Ativar ou desativar categorias inteligentes",
                    icon = Icons.Rounded.Category,
                    isExpanded = expandedSection == "drawer",
                    onToggle = {
                        expandedSection = if (expandedSection == "drawer") null else "drawer"
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingsSwitchRow(
                            title = "Categorias Automáticas na Gaveta",
                            subtitle = if (uiState.drawerCategorized)
                                "Ativado: Exibe abas por categoria inteligente (Comunicação, Mídia, Produtividade)"
                            else
                                "Desativado: Exibe todos os apps em lista única e contínua em ordem alfabética",
                            checked = uiState.drawerCategorized,
                            onCheckedChange = { viewModel.setDrawerCategorized(it) }
                        )
                    }
                }
            }

            // 6. RECURSOS DE INTELIGÊNCIA ARTIFICIAL
            item {
                SettingsCategorySection(
                    title = "Inteligência Artificial (IA)",
                    subtitle = "Sugestões contextuais e rotinas diárias automáticas",
                    icon = Icons.Rounded.AutoAwesome,
                    isExpanded = expandedSection == "ai",
                    onToggle = {
                        expandedSection = if (expandedSection == "ai") null else "ai"
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingsSwitchRow(
                            title = "Ativar Inteligência Artificial no Launcher",
                            subtitle = if (uiState.isAiEnabled)
                                "IA ativada: Gera resumos matinais e sugestões dos apps mais relevantes para o momento."
                            else
                                "IA desativada: O launcher funciona em modo leve sem processamento inteligente.",
                            checked = uiState.isAiEnabled,
                            onCheckedChange = { viewModel.setAiEnabled(it) }
                        )
                    }
                }
            }

            // 7. GESTOS E AÇÕES RÁPIDAS
            item {
                SettingsCategorySection(
                    title = "Gestos e Atalhos",
                    subtitle = "Personalize toque duplo e deslize para baixo",
                    icon = Icons.Rounded.Gesture,
                    isExpanded = expandedSection == "gestures",
                    onToggle = {
                        expandedSection = if (expandedSection == "gestures") null else "gestures"
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Ação ao Deslizar para Baixo:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val swipeActions = listOf(
                                GestureAction.SEARCH to "Pesquisa",
                                GestureAction.NOTIFICATIONS to "Notificações",
                                GestureAction.AI_ROUTINE to "Rotina IA"
                            )
                            swipeActions.forEach { (action, name) ->
                                val isSelected = uiState.gestureSwipeDown == action
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setGestureSwipeDown(action) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = name,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Ação ao Toque Duplo na Tela:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val doubleTapActions = listOf(
                                GestureAction.AI_ROUTINE to "Rotina IA",
                                GestureAction.APP_DRAWER to "Gaveta",
                                GestureAction.CUSTOMIZE to "Personalizar"
                            )
                            doubleTapActions.forEach { (action, name) ->
                                val isSelected = uiState.gestureDoubleTap == action
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setGestureDoubleTap(action) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = name,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 8. SISTEMA E LAUNCHER PADRÃO
            item {
                SettingsCategorySection(
                    title = "Sistema & Padrão",
                    subtitle = "Definir como launcher principal e taxa de 120Hz",
                    icon = Icons.Rounded.Speed,
                    isExpanded = expandedSection == "system",
                    onToggle = {
                        expandedSection = if (expandedSection == "system") null else "system"
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onRequestDefaultHome,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Definir Front Launcher como Padrão", fontWeight = FontWeight.Bold)
                        }

                        SettingsSwitchRow(
                            title = "Alta Taxa de Atualização (120Hz)",
                            subtitle = "Forçar renderização na maior taxa de quadros suportada pela tela",
                            checked = uiState.highRefreshRateEnabled,
                            onCheckedChange = { viewModel.setHighRefreshRateEnabled(it) }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Modo de Renderização", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Text("Escolha entre GPU Real, Simulado ou Software para otimizar desempenho e compatibilidade.", color = Color.Gray, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modes = listOf("GPU_REAL" to "GPU Real", "SIMULATED" to "Simulado", "SOFTWARE" to "Software")
                            modes.forEach { (key, label) ->
                                val isSelected = uiState.renderingMode == key
                                Button(
                                    onClick = { viewModel.setRenderingMode(key) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFF2563EB) else Color(0xFF374151)
                                    ),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.setWelcomeOnboardingOpen(true) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reabrir Assistente de Configuração Inicial", color = Color.White)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(28.dp)) }
        }
    }
}

@Composable
fun SettingsCategorySection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF161B22)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        lineHeight = 14.sp
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(14.dp))
                    content()
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}
