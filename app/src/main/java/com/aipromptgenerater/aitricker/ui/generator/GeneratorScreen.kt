package com.aipromptgenerater.aitricker.ui.generator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
fun ThemeSelector(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dark AI Style Card
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onThemeSelected("Dark AI Style") }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E085E), Color(0xFF6B11FF))
                        )
                    )
                    .border(
                        BorderStroke(
                            width = if (selectedTheme == "Dark AI Style") 2.dp else 1.dp,
                            color = if (selectedTheme == "Dark AI Style") MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dark AI Style",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTheme == "Dark AI Style") MaterialTheme.colorScheme.onSurface else Color.Gray
                )
                if (selectedTheme == "Dark AI Style") {
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

        // Light Pro Style Card
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onThemeSelected("Light Pro Style") }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFE8F5E9), Color(0xFF81C784))
                        )
                    )
                    .border(
                        BorderStroke(
                            width = if (selectedTheme == "Light Pro Style") 2.dp else 1.dp,
                            color = if (selectedTheme == "Light Pro Style") MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Light Pro Style",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTheme == "Light Pro Style") MaterialTheme.colorScheme.onSurface else Color.Gray
                )
                if (selectedTheme == "Light Pro Style") {
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

    var uiTheme by remember { mutableStateOf("Dark AI Style") }

    var paymentGateway by remember { mutableStateOf("Razorpay") }
    var isPaymentHelpExpanded by remember { mutableStateOf(false) }

    var aiIntegration by remember { mutableStateOf("No AI Integration") }

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
    val finalTechStack = remember(uiTheme, paymentGateway, aiIntegration) {
        "UI Theme: $uiTheme | Payment Gateway: $paymentGateway | AI Integration: $aiIntegration"
    }
    val finalFeatures = remember(panelType, authSystem) {
        "Panel Type: $panelType | Login System: $authSystem"
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
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("AI Generated Prompt", state.promptHistory.response)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
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
                            uiTheme = "Dark AI Style"
                            paymentGateway = "Razorpay"
                            aiIntegration = "No AI Integration"
                            projectIdea = ""
                            extraFeatures = ""
                            isPreviewShown = false
                            viewModel.resetState()
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

                            // 6. AI Integration Card
                            OptionGroupCard(
                                icon = Icons.Default.Star,
                                title = "AI Integration"
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SegmentedOptionRow(
                                        options = listOf("No AI Integration", "Gemini (Google)"),
                                        selectedOption = aiIntegration,
                                        onOptionSelected = { aiIntegration = it }
                                    )
                                    SegmentedOptionRow(
                                        options = listOf("OpenRouter", "ChatGPT (OpenAI)"),
                                        selectedOption = aiIntegration,
                                        onOptionSelected = { aiIntegration = it }
                                    )
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

                                    val details = listOf(
                                        "Project Type" to generatorType,
                                        "Name" to projectName.ifEmpty { "Not specified" },
                                        (if (generatorType == "App") "Package Name" else "Domain Name") to packageOrDomain.ifEmpty { "Not specified" },
                                        "Panel Type" to panelType,
                                        "Auth System" to authSystem,
                                        "UI Theme" to uiTheme,
                                        "Payment Gateway" to paymentGateway,
                                        "AI Integration" to aiIntegration,
                                        "Description" to projectIdea
                                    )

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
    onNew: () -> Unit
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
