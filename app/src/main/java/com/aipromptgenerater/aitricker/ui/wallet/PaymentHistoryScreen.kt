package com.aipromptgenerater.aitricker.ui.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: WalletViewModel = viewModel { WalletViewModel() }
) {
    val context = LocalContext.current
    val history by viewModel.paymentHistory.collectAsState()
    var selectedReceipt by remember { mutableStateOf<Map<String, Any>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment History", fontWeight = FontWeight.Bold) },
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
            if (history.isEmpty()) {
                EmptyHistoryView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(history) { receipt ->
                        ReceiptHistoryCard(
                            receipt = receipt,
                            onClick = { selectedReceipt = receipt }
                        )
                    }
                }
            }
        }
    }

    // Receipt Detail Invoice Modal Dialog
    selectedReceipt?.let { receipt ->
        ReceiptDetailsDialog(
            receipt = receipt,
            onDismiss = { selectedReceipt = null },
            context = context
        )
    }
}

@Composable
fun EmptyHistoryView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val gradient = Brush.radialGradient(
            colors = listOf(Color(0xFF2563EB).copy(alpha = 0.15f), Color.Transparent)
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = "No receipts",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Transactions Yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Any credit packages you purchase will be logged here dynamically.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun ReceiptHistoryCard(
    receipt: Map<String, Any>,
    onClick: () -> Unit
) {
    val credits = (receipt["creditsPurchased"] as? Number)?.toInt() ?: 0
    val timestamp = (receipt["timestamp"] as? Number)?.toLong() ?: 0L
    val orderId = receipt["orderId"] as? String ?: "N/A"
    
    // Determine plan type label based on credits added
    val planLabel = when (credits) {
        499 -> "Basic Plan"
        199 -> "Basic Plan"
        1599 -> "Most Popular"
        2999 -> "Premium Creator"
        else -> "Credit Package"
    }

    // Retrieve pricePaid if available, else estimate based on plan credits
    val price = (receipt["pricePaid"] as? Number)?.toInt() ?: when (credits) {
        499 -> 99
        199 -> 99
        1599 -> 299
        2999 -> 499
        else -> 0
    }

    val formattedDate = remember(timestamp) {
        if (timestamp > 0) {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else {
            "N/A"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = planLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        contentColor = Color(0xFF10B981),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "SUCCESS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (price > 0) "₹$price" else "Paid",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "+$credits Credits",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D9488)
                )
            }
        }
    }
}

@Composable
fun ReceiptDetailsDialog(
    receipt: Map<String, Any>,
    onDismiss: () -> Unit,
    context: Context
) {
    val credits = (receipt["creditsPurchased"] as? Number)?.toInt() ?: 0
    val timestamp = (receipt["timestamp"] as? Number)?.toLong() ?: 0L
    val orderId = receipt["orderId"] as? String ?: "N/A"
    val receiptId = receipt["receiptId"] as? String ?: "N/A"
    val gateway = receipt["gateway"] as? String ?: "Razorpay"

    val planLabel = when (credits) {
        499 -> "Basic Plan"
        199 -> "Basic Plan"
        1599 -> "Most Popular"
        2999 -> "Premium Creator"
        else -> "Credit Package"
    }

    val price = (receipt["pricePaid"] as? Number)?.toInt() ?: when (credits) {
        499 -> 99
        199 -> 99
        1599 -> 299
        2999 -> 499
        else -> 0
    }

    val formattedDateTime = remember(timestamp) {
        if (timestamp > 0) {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else {
            "N/A"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Transaction Receipt",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Package Header Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = planLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Credits credited: +$credits",
                            fontSize = 12.sp,
                            color = Color(0xFF0D9488),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = if (price > 0) "₹$price" else "Paid",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Meta fields
                ReceiptMetaItem(label = "Payment Status", value = "SUCCESSFUL", isStatus = true)
                ReceiptMetaItem(label = "Date & Time", value = formattedDateTime)
                ReceiptMetaItem(label = "Payment Gateway", value = gateway)
                
                ReceiptMetaItem(
                    label = "Razorpay Order ID",
                    value = orderId,
                    showCopy = true,
                    context = context
                )
                ReceiptMetaItem(
                    label = "App Receipt ID",
                    value = receiptId,
                    showCopy = true,
                    context = context
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ReceiptMetaItem(
    label: String,
    value: String,
    isStatus: Boolean = false,
    showCopy: Boolean = false,
    context: Context? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isStatus) {
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    contentColor = Color(0xFF10B981),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = value,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            } else {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            if (showCopy && context != null && value != "N/A" && value.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(label, value)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "$label copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
