package com.aipromptgenerater.aitricker.ui.generator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Canvas
import android.content.ContentValues
import android.provider.MediaStore
import android.net.Uri
import android.os.Environment
import android.text.TextPaint
import android.text.StaticLayout
import android.text.Layout
import java.io.OutputStream
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aipromptgenerater.aitricker.ui.components.GeminiLoadingState
import com.aipromptgenerater.aitricker.ui.components.GradientButton
import com.aipromptgenerater.aitricker.ui.components.PremiumCard
import com.aipromptgenerater.aitricker.ui.utils.copyToClipboard
import com.aipromptgenerater.aitricker.ui.utils.shareToWhatsApp
import com.aipromptgenerater.aitricker.ui.utils.savePromptAsPdf

@Composable
fun OptionGroupCard(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
fun SegmentedOptionRow(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Button(
                onClick = { onOptionSelected(option) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color(0xFFB2DFDB) else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isSelected) Color(0xFF004D40) else Color(0xFFCCCCCC)),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color(0xFF004D40),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = option,
                        color = if (isSelected) Color(0xFF004D40) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HelpTextDropdown(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable { onToggle() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Toggle Info",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "What's included?",
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                text = content,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 20.dp, top = 2.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
fun ToggleChecklistRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

data class DesignTheme(
    val name: String,
    val gradient: Brush
)

@Composable
fun ThemeSelector(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themes = remember {
        listOf(
            DesignTheme(
                name = "Let AI Choose (Skip)",
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121))
                )
            ),
            DesignTheme(
                name = "Dark AI Cyberpunk",
                gradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E085E), Color(0xFF6B11FF))
                )
            ),
            DesignTheme(
                name = "Light Pro Clean",
                gradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8F5E9), Color(0xFF81C784))
                )
            ),
            DesignTheme(
                name = "Glassmorphism Blue",
                gradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE0F7FA), Color(0xFF00B0FF))
                )
            ),
            DesignTheme(
                name = "Sunset Warmth",
                gradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF9100), Color(0xFFFF4081))
                )
            ),
            DesignTheme(
                name = "Minimal Slate",
                gradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFF78909C), Color(0xFF263238))
                )
            ),
            DesignTheme(
                name = "Emerald Gold",
                gradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2E7D32), Color(0xFFFFD54F))
                )
            ),
            DesignTheme(
                name = "Royal Orchid",
                gradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFF8E24AA), Color(0xFFF06292))
                )
            ),
            DesignTheme(
                name = "Ocean Breeze",
                gradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00ACC1), Color(0xFF0D47A1))
                )
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val rows = themes.chunked(2)
        rows.forEach { rowThemes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowThemes.forEach { theme ->
                    val isSkipOption = theme.name == "Let AI Choose (Skip)"
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onThemeSelected(theme.name) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (isSkipOption) {
                                        Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    } else {
                                        Modifier.background(theme.gradient)
                                    }
                                )
                                .border(
                                    BorderStroke(
                                        width = if (selectedTheme == theme.name) 2.dp else 1.dp,
                                        color = if (selectedTheme == theme.name) {
                                            MaterialTheme.colorScheme.primary
                                        } else if (isSkipOption) {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                        } else {
                                            Color.Transparent
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSkipOption) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSkipOption) "Skip (Let AI Choose)" else theme.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTheme == theme.name) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            if (selectedTheme == theme.name) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
                if (rowThemes.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    generatorType: String, // "Website" or "App"
    onNavigateBack: () -> Unit,
    viewModel: GeneratorViewModel = viewModel { GeneratorViewModel() }
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var projectName by remember { mutableStateOf("") }
    var packageOrDomain by remember { mutableStateOf("") }

    // Selection states
    var panelType by remember { mutableStateOf("Only User") }
    var isPanelHelpExpanded by remember { mutableStateOf(false) }

    var authSystem by remember { mutableStateOf("Google Login") }
    var isAuthHelpExpanded by remember { mutableStateOf(false) }

    var uiTheme by remember { mutableStateOf("Let AI Choose (Skip)") }

    var paymentGateway by remember { mutableStateOf("Razorpay") }
    var isPaymentHelpExpanded by remember { mutableStateOf(false) }

    var monetizationModel by remember { mutableStateOf("") }
    var isMonetizationHelpExpanded by remember { mutableStateOf(false) }

    var aiIntegration by remember { mutableStateOf("Let AI Choose (Skip)") }

    var preferredTechStack by remember { mutableStateOf("") }

    // Advanced optional settings
    var targetPlatform by remember { mutableStateOf("Not Specified") }
    var databasePreference by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("") }
    var hasMaps by remember { mutableStateOf(false) }
    var hasCamera by remember { mutableStateOf(false) }
    var hasNotifications by remember { mutableStateOf(false) }
    var hasAnalytics by remember { mutableStateOf(false) }
    var isAdvancedSettingsExpanded by remember { mutableStateOf(false) }

    var projectIdea by remember { mutableStateOf("") }

    // Preview state
    var isPreviewShown by remember { mutableStateOf(false) }

    // Extra features input
    var extraFeatures by remember { mutableStateOf("") }

    // Compiled prompts for AI consumption
    val finalName = remember(projectName, packageOrDomain) {
        if (packageOrDomain.isNotEmpty()) {
            "$projectName (${if (generatorType == "App") "Package" else "Domain"}: $packageOrDomain)"
        } else {
            projectName
        }
    }
    val finalTechStack = remember(uiTheme, paymentGateway, aiIntegration, preferredTechStack, databasePreference, targetPlatform) {
        val base = StringBuilder().apply {
            append("UI Theme: $uiTheme | Payment Gateway: $paymentGateway | AI Integration: $aiIntegration")
            if (preferredTechStack.trim().isNotEmpty()) {
                append(" | Preferred Tech Stack: $preferredTechStack")
            }
            if (databasePreference.isNotEmpty() && databasePreference != "Not Specified") {
                append(" | Database Preference: $databasePreference")
            }
            if (generatorType == "App" && targetPlatform != "Not Specified") {
                append(" | Target Platform: $targetPlatform")
            }
        }.toString()
        base
    }
    val finalFeatures = remember(panelType, authSystem, monetizationModel, targetAudience, hasMaps, hasCamera, hasNotifications, hasAnalytics) {
        val base = StringBuilder().apply {
            append("Panel Type: $panelType | Login System: $authSystem")
            if (monetizationModel.isNotEmpty()) {
                append(" | Monetization Model: $monetizationModel")
            }
            if (targetAudience.isNotEmpty() && targetAudience != "Not Specified") {
                append(" | Target Audience: $targetAudience")
            }
            val integrations = mutableListOf<String>()
            if (hasMaps) integrations.add("Maps & Geolocation")
            if (hasCamera) integrations.add("Camera & Media Uploads")
            if (hasNotifications) integrations.add("Push Notifications")
            if (hasAnalytics) integrations.add("Analytics & Crash Reports")
            if (integrations.isNotEmpty()) {
                append(" | Integrations: ${integrations.joinToString(", ")}")
            }
        }.toString()
        base
    }
    val finalIdea = remember(projectIdea, extraFeatures) {
        if (extraFeatures.trim().isNotEmpty()) {
            "$projectIdea\n\nExtra Requested Features:\n$extraFeatures"
        } else {
            projectIdea
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Generate Prompt for $generatorType", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is GeneratorUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        GeminiLoadingState()
                    }
                }
                is GeneratorUiState.Success -> {
                    ResponseView(
                        promptText = state.promptHistory.response,
                        creditsRemaining = userProfile?.credits ?: 0,
                        onCopy = {
                            copyToClipboard(context, state.promptHistory.response)
                        },
                        onRegenerate = {
                            viewModel.generatePrompt(
                                type = generatorType,
                                name = finalName,
                                idea = finalIdea,
                                techStack = finalTechStack,
                                features = finalFeatures
                            )
                        },
                        onNew = {
                            projectName = ""
                            packageOrDomain = ""
                            panelType = "Only User"
                            authSystem = "Google Login"
                            uiTheme = "Let AI Choose (Skip)"
                            paymentGateway = "Razorpay"
                            monetizationModel = ""
                            isMonetizationHelpExpanded = false
                            preferredTechStack = ""
                            targetPlatform = "Not Specified"
                            databasePreference = ""
                            targetAudience = ""
                            hasMaps = false
                            hasCamera = false
                            hasNotifications = false
                            hasAnalytics = false
                            isAdvancedSettingsExpanded = false
                            aiIntegration = "Let AI Choose (Skip)"
                            projectIdea = ""
                            extraFeatures = ""
                            isPreviewShown = false
                            viewModel.resetState()
                        },
                        onShareWhatsApp = {
                            shareToWhatsApp(context, state.promptHistory.response)
                        },
                        onDownloadPdf = {
                            val title = if (generatorType == "App") "App Prompt Architecture" else "Website Prompt Architecture"
                            savePromptAsPdf(context, title, state.promptHistory.response)
                        }
                    )
                }
                else -> {
                    // Form Input View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Credit Wallet Section
                        val walletCredits = userProfile?.credits ?: 0
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (walletCredits >= 5) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                }
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Wallet: $walletCredits Credits • 5 Credits will be deducted per generation.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (walletCredits >= 5) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    }
                                )
                            }
                        }

                        if (!isPreviewShown) {
                            // 1. Identity Card
                            OptionGroupCard(
                                icon = Icons.Default.Edit,
                                title = if (generatorType == "App") "App Identity" else "Website Identity"
                            ) {
                                OutlinedTextField(
                                    value = projectName,
                                    onValueChange = { projectName = it },
                                    label = { Text(if (generatorType == "App") "App Name" else "Website Name") },
                                    placeholder = { Text(if (generatorType == "App") "e.g. FitTrack Tracker" else "e.g. FitTrack Portal") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = packageOrDomain,
                                    onValueChange = { packageOrDomain = it },
                                    label = { Text(if (generatorType == "App") "Package Name (e.g. com.yourname.appname)" else "Domain Name (e.g. yourname.com)") },
                                    placeholder = { Text(if (generatorType == "App") "com.yourname.appname" else "yourname.com") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true
                                )
                            }

                            // 2. Panel Type Card
                            OptionGroupCard(
                                icon = Icons.Default.Info,
                                title = "Panel Type"
                            ) {
                                SegmentedOptionRow(
                                    options = listOf("Admin + User", "Only User"),
                                    selectedOption = panelType,
                                    onOptionSelected = { panelType = it }
                                )
                                HelpTextDropdown(
                                    isExpanded = isPanelHelpExpanded,
                                    onToggle = { isPanelHelpExpanded = !isPanelHelpExpanded },
                                    content = if (panelType == "Admin + User") {
                                        "Includes a dedicated admin portal to manage app data, users, and global settings, plus the standard user/customer app interface."
                                    } else {
                                        "Only the primary user/customer application interface is built. No back-office administrative panel included."
                                    }
                                )
                            }

                            // 3. Login / Auth System Card
                            OptionGroupCard(
                                icon = Icons.Default.Lock,
                                title = "Login / Auth System"
                            ) {
                                SegmentedOptionRow(
                                    options = listOf("Google Login", "No Login System"),
                                    selectedOption = authSystem,
                                    onOptionSelected = { authSystem = it }
                                )
                                HelpTextDropdown(
                                    isExpanded = isAuthHelpExpanded,
                                    onToggle = { isAuthHelpExpanded = !isAuthHelpExpanded },
                                    content = if (authSystem == "Google Login") {
                                        "Integrates Google Sign-In and profile creation, allowing users to log in securely using their Google accounts."
                                    } else {
                                        "Allows access to all application screens and features immediately without authentication or profile creation."
                                    }
                                )
                            }

                            // 4. UI Design Theme Card
                            OptionGroupCard(
                                icon = Icons.Default.Palette,
                                title = "UI Design Theme"
                            ) {
                                ThemeSelector(
                                    selectedTheme = uiTheme,
                                    onThemeSelected = { uiTheme = it }
                                )
                            }

                            // 5. Payment Gateway Card
                            OptionGroupCard(
                                icon = Icons.Default.ShoppingCart,
                                title = "Payment Gateway"
                            ) {
                                SegmentedOptionRow(
                                    options = listOf("Cashfree", "Razorpay", "Without Payment"),
                                    selectedOption = paymentGateway,
                                    onOptionSelected = { paymentGateway = it }
                                )
                                HelpTextDropdown(
                                    isExpanded = isPaymentHelpExpanded,
                                    onToggle = { isPaymentHelpExpanded = !isPaymentHelpExpanded },
                                    content = when (paymentGateway) {
                                        "Cashfree" -> "Integrates Cashfree payment gateway APIs to handle card, UPI, netbanking, and wallet payments."
                                        "Razorpay" -> "Integrates Razorpay checkout APIs to handle credit/debit cards, UPI, netbanking, and wallet transactions."
                                        else -> "No payment processing gateway is integrated. All billing or checkout functionalities are disabled."
                                    }
                                )
                            }

                            // 5.5 Monetization Model Card
                            OptionGroupCard(
                                icon = Icons.Default.AttachMoney,
                                title = "Monetization Model (Optional)"
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SegmentedOptionRow(
                                        options = listOf("Let AI Choose (Skip)", "Credit Pack (Basic / Pro / Premium)"),
                                        selectedOption = monetizationModel,
                                        onOptionSelected = { selected ->
                                            monetizationModel = if (monetizationModel == selected) "" else selected
                                        }
                                    )
                                    SegmentedOptionRow(
                                        options = listOf("Product Based (like Amazon)", "Service Based (Appointments / Booking)"),
                                        selectedOption = monetizationModel,
                                        onOptionSelected = { selected ->
                                            monetizationModel = if (monetizationModel == selected) "" else selected
                                        }
                                    )
                                }
                                HelpTextDropdown(
                                    isExpanded = isMonetizationHelpExpanded,
                                    onToggle = { isMonetizationHelpExpanded = !isMonetizationHelpExpanded },
                                    content = when (monetizationModel) {
                                        "Let AI Choose (Skip)" -> "Analyzes your project idea to recommend the most optimal monetization model (credits, products, or appointments)."
                                        "Credit Pack (Basic / Pro / Premium)" -> "Allows users to buy credit packages to consume services inside the app. Ideal for token-based, AI usage, or query-based products."
                                        "Product Based (like Amazon)" -> "Includes a shopping cart, catalog, checkouts, and order management to sell physical or digital inventory."
                                        else -> "Enables appointment scheduling, calendar integrations, and booking workflows to sell time slots or professional services."
                                    }
                                )
                            }

                            // 6. AI Integration Card
                            OptionGroupCard(
                                icon = Icons.Default.Star,
                                title = "AI Integration"
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SegmentedOptionRow(
                                        options = listOf("Let AI Choose (Skip)", "No AI Integration"),
                                        selectedOption = aiIntegration,
                                        onOptionSelected = { aiIntegration = it }
                                    )
                                    SegmentedOptionRow(
                                        options = listOf("Gemini (Google)", "ChatGPT (OpenAI)"),
                                        selectedOption = aiIntegration,
                                        onOptionSelected = { aiIntegration = it }
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        val isSelected = aiIntegration == "OpenRouter"
                                        Button(
                                            onClick = { aiIntegration = "OpenRouter" },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected) Color(0xFFB2DFDB) else Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF004D40) else Color(0xFFCCCCCC)),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = Color(0xFF004D40),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(
                                                    text = "OpenRouter",
                                                    color = if (isSelected) Color(0xFF004D40) else Color.Gray,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            // 6.5 Preferred Tech Stack Card (Optional)
                            OptionGroupCard(
                                icon = Icons.Default.Build,
                                title = "Preferred Tech Stack (Optional)"
                            ) {
                                OutlinedTextField(
                                    value = preferredTechStack,
                                    onValueChange = { preferredTechStack = it },
                                    label = { Text("Technologies / Frameworks") },
                                    placeholder = { 
                                        Text(
                                            if (generatorType == "App") {
                                                "e.g. Kotlin, Jetpack Compose, Firebase"
                                            } else {
                                                "e.g. React, Next.js, TailwindCSS, Node.js"
                                            }
                                        ) 
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true
                                )
                            }

                            // Advanced Settings Header
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isAdvancedSettingsExpanded = !isAdvancedSettingsExpanded }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Advanced Platform & Infrastructure (Optional)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (isAdvancedSettingsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle Advanced",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isAdvancedSettingsExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // 1. Target Platforms (Mobile App Specific)
                                    if (generatorType == "App") {
                                        OptionGroupCard(
                                            icon = Icons.Default.Build,
                                            title = "Target Platform (Optional)"
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                SegmentedOptionRow(
                                                    options = listOf("Not Specified", "Android Native (Kotlin)"),
                                                    selectedOption = targetPlatform,
                                                    onOptionSelected = { targetPlatform = it }
                                                )
                                                SegmentedOptionRow(
                                                    options = listOf("iOS Native (Swift)", "Flutter"),
                                                    selectedOption = targetPlatform,
                                                    onOptionSelected = { targetPlatform = it }
                                                )
                                                SegmentedOptionRow(
                                                    options = listOf("React Native"),
                                                    selectedOption = targetPlatform,
                                                    onOptionSelected = { targetPlatform = it }
                                                )
                                            }
                                        }
                                    }

                                    // 2. Database & Hosting Preference
                                    OptionGroupCard(
                                        icon = Icons.Default.Info,
                                        title = "Database & Hosting Preference (Optional)"
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            SegmentedOptionRow(
                                                options = listOf("Not Specified", "Firebase Firestore"),
                                                selectedOption = databasePreference,
                                                onOptionSelected = { selected ->
                                                    databasePreference = if (databasePreference == selected) "" else selected
                                                }
                                            )
                                            SegmentedOptionRow(
                                                options = listOf("Supabase (Postgres)", "Custom API (SQL)"),
                                                selectedOption = databasePreference,
                                                onOptionSelected = { selected ->
                                                    databasePreference = if (databasePreference == selected) "" else selected
                                                }
                                            )
                                            SegmentedOptionRow(
                                                options = listOf("Local Only"),
                                                selectedOption = databasePreference,
                                                onOptionSelected = { selected ->
                                                    databasePreference = if (databasePreference == selected) "" else selected
                                                }
                                            )
                                        }
                                    }

                                    // 3. Target Audience / User Type
                                    OptionGroupCard(
                                        icon = Icons.Default.Info,
                                        title = "Target Audience (Optional)"
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            SegmentedOptionRow(
                                                options = listOf("Not Specified", "B2C (Public Users)"),
                                                selectedOption = targetAudience,
                                                onOptionSelected = { selected ->
                                                    targetAudience = if (targetAudience == selected) "" else selected
                                                }
                                            )
                                            SegmentedOptionRow(
                                                options = listOf("B2B (Enterprise)", "Mixed Audience"),
                                                selectedOption = targetAudience,
                                                onOptionSelected = { selected ->
                                                    targetAudience = if (targetAudience == selected) "" else selected
                                                }
                                            )
                                        }
                                    }

                                    // 4. Advanced Integrations (Multi-Select Checklist)
                                    OptionGroupCard(
                                        icon = Icons.Default.Add,
                                        title = "Advanced Integrations (Optional)"
                                    ) {
                                        Column {
                                            ToggleChecklistRow(
                                                label = "Maps & Geolocation",
                                                checked = hasMaps,
                                                onCheckedChange = { hasMaps = it }
                                            )
                                            ToggleChecklistRow(
                                                label = "Camera & Media Uploads",
                                                checked = hasCamera,
                                                onCheckedChange = { hasCamera = it }
                                            )
                                            ToggleChecklistRow(
                                                label = "Push Notifications",
                                                checked = hasNotifications,
                                                onCheckedChange = { hasNotifications = it }
                                            )
                                            ToggleChecklistRow(
                                                label = "Analytics & Crash Reports",
                                                checked = hasAnalytics,
                                                onCheckedChange = { hasAnalytics = it }
                                            )
                                        }
                                    }
                                }
                            }

                            // 7. Describe Card
                            OptionGroupCard(
                                icon = Icons.Default.Info,
                                title = if (generatorType == "App") "Describe Your App Idea" else "Describe Your Website Idea"
                            ) {
                                OutlinedTextField(
                                    value = projectIdea,
                                    onValueChange = { projectIdea = it },
                                    placeholder = { Text("Describe your ${generatorType.lowercase()} idea in detail (Hindi, English, or Hinglish)...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    minLines = 4
                                )
                                val wordCount = projectIdea.trim().let {
                                    if (it.isEmpty()) 0 else it.split(Regex("\\s+")).count { w -> w.isNotEmpty() }
                                }
                                Text(
                                    text = "$wordCount / 500 words",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (state is GeneratorUiState.Error) {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Preview Button
                            Button(
                                onClick = {
                                    if (projectName.trim().isEmpty()) {
                                        Toast.makeText(context, "Please enter a Name.", Toast.LENGTH_SHORT).show()
                                    } else if (projectIdea.trim().isEmpty()) {
                                        Toast.makeText(context, "Please enter your idea description.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        isPreviewShown = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Preview",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Preview My Prompt Details", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Preview Mode is Active
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "Your Prompt Configuration Summary",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

                                    val details = remember(projectName, packageOrDomain, panelType, authSystem, uiTheme, paymentGateway, monetizationModel, aiIntegration, preferredTechStack, targetPlatform, databasePreference, targetAudience, hasMaps, hasCamera, hasNotifications, hasAnalytics, projectIdea) {
                                        val list = mutableListOf(
                                            "Project Type" to generatorType,
                                            "Name" to projectName.ifEmpty { "Not specified" },
                                            (if (generatorType == "App") "Package Name" else "Domain Name") to packageOrDomain.ifEmpty { "Not specified" },
                                            "Panel Type" to panelType,
                                            "Auth System" to authSystem,
                                            "UI Theme" to uiTheme,
                                            "Payment Gateway" to paymentGateway,
                                            "Monetization Model" to monetizationModel.ifEmpty { "Not specified" },
                                            "AI Integration" to aiIntegration
                                        )
                                        if (preferredTechStack.trim().isNotEmpty()) {
                                            list.add("Preferred Tech Stack" to preferredTechStack)
                                        }
                                        if (generatorType == "App" && targetPlatform != "Not Specified") {
                                            list.add("Target Platform" to targetPlatform)
                                        }
                                        if (databasePreference.isNotEmpty() && databasePreference != "Not Specified") {
                                            list.add("Database & Hosting" to databasePreference)
                                        }
                                        if (targetAudience.isNotEmpty() && targetAudience != "Not Specified") {
                                            list.add("Target Audience" to targetAudience)
                                        }
                                        val integrations = mutableListOf<String>()
                                        if (hasMaps) integrations.add("Maps")
                                        if (hasCamera) integrations.add("Camera")
                                        if (hasNotifications) integrations.add("Notifications")
                                        if (hasAnalytics) integrations.add("Analytics")
                                        if (integrations.isNotEmpty()) {
                                            list.add("Integrations" to integrations.joinToString(", "))
                                        }
                                        list.add("Description" to projectIdea)
                                        list
                                    }

                                    details.forEach { (label, value) ->
                                        Column {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                            Text(
                                                text = value,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Edit Details option
                            OutlinedButton(
                                onClick = { isPreviewShown = false },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Options",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Configuration Details")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Extra Features Card (only visible in preview step)
                            OptionGroupCard(
                                icon = Icons.Default.Add,
                                title = "Extra Features (optional)"
                            ) {
                                OutlinedTextField(
                                    value = extraFeatures,
                                    onValueChange = { extraFeatures = it },
                                    placeholder = { Text("Add any extra features or notes for AI...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    minLines = 2
                                )
                            }

                            if (state is GeneratorUiState.Error) {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Structured Prompt Generation Button
                            GradientButton(
                                text = "Generate Structured Prompt (-5 Credits)",
                                onClick = {
                                    viewModel.generatePrompt(
                                        type = generatorType,
                                        name = finalName,
                                        idea = finalIdea,
                                        techStack = finalTechStack,
                                        features = finalFeatures
                                    )
                                },
                                enabled = walletCredits >= 5
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun ResponseView(
    promptText: String,
    creditsRemaining: Int,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onNew: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onDownloadPdf: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Output result card
        PremiumCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Generated Prompt Architecture",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = promptText,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons Row
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCopy,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Copy Prompt")
                }

                OutlinedButton(
                    onClick = {
                        // Regenerate will deduct credits. Simple local warning, then execute
                        onRegenerate()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Regenerate (-5)")
                }
            }

            // Share & PDF Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onShareWhatsApp,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp Green
                ) {
                    Text("WhatsApp", color = Color.White)
                }

                OutlinedButton(
                    onClick = onDownloadPdf,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Download PDF")
                }
            }

            OutlinedButton(
                onClick = onNew,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Generate Another")
            }

            Text(
                text = "Credits remaining: $creditsRemaining",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
