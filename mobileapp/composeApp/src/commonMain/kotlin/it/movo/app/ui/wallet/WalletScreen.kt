package it.movo.app.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import it.movo.app.data.model.PaymentMethod
import it.movo.app.data.model.PaymentMethodType
import it.movo.app.ui.theme.MovoOnSurface
import it.movo.app.ui.theme.MovoOnSurfaceVariant
import it.movo.app.ui.theme.MovoOutline
import it.movo.app.ui.theme.MovoSurface
import it.movo.app.ui.theme.MovoSurfaceVariant
import it.movo.app.ui.theme.MovoTeal
import it.movo.app.ui.theme.MovoTheme
import it.movo.app.ui.theme.MovoWhite
import it.movo.app.composeapp.generated.resources.Res
import it.movo.app.composeapp.generated.resources.back
import it.movo.app.composeapp.generated.resources.nav_wallet
import it.movo.app.composeapp.generated.resources.wallet_add_new_card
import it.movo.app.composeapp.generated.resources.wallet_cancel
import it.movo.app.composeapp.generated.resources.wallet_card_number
import it.movo.app.composeapp.generated.resources.wallet_cvv
import it.movo.app.composeapp.generated.resources.wallet_default
import it.movo.app.composeapp.generated.resources.wallet_empty
import it.movo.app.composeapp.generated.resources.wallet_empty_subtitle
import it.movo.app.composeapp.generated.resources.wallet_expires
import it.movo.app.composeapp.generated.resources.wallet_expiry_date
import it.movo.app.composeapp.generated.resources.wallet_payments_secured
import it.movo.app.composeapp.generated.resources.wallet_save
import it.movo.app.composeapp.generated.resources.wallet_set_default
import it.movo.app.composeapp.generated.resources.wallet_subtitle
import it.movo.app.composeapp.generated.resources.wallet_title
import it.movo.app.composeapp.generated.resources.wallet_view_transactions
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel,
    onNavigateBack: () -> Unit,
    onViewTransactions: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // TODO: Integrate with a real payment provider (e.g. Stripe) for card tokenization
    if (uiState.showAddCardDialog) {
        var cardNumber by remember { mutableStateOf("") }
        var expiryDate by remember { mutableStateOf("") }
        var cvv by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.dismissAddCardDialog() },
            title = { Text(stringResource(Res.string.wallet_add_new_card)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text(stringResource(Res.string.wallet_card_number)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text(stringResource(Res.string.wallet_expiry_date)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { cvv = it },
                        label = { Text(stringResource(Res.string.wallet_cvv)) },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.addCard(cardNumber, expiryDate, cvv) },
                    enabled = cardNumber.isNotBlank() && expiryDate.isNotBlank() && cvv.isNotBlank()
                ) {
                    Text(stringResource(Res.string.wallet_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissAddCardDialog() }) {
                    Text(stringResource(Res.string.wallet_cancel))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.nav_wallet),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back),
                            tint = MovoOnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MovoSurface
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        WalletContent(
            uiState = uiState,
            paddingValues = paddingValues,
            onSetDefault = viewModel::setDefaultMethod,
            onDelete = viewModel::deleteMethod,
            onViewTransactions = onViewTransactions,
            onAddCard = viewModel::showAddCardDialog
        )
    }
}

@Composable
private fun WalletContent(
    uiState: WalletUiState,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    onSetDefault: (String) -> Unit,
    onDelete: (String) -> Unit,
    onViewTransactions: () -> Unit,
    onAddCard: () -> Unit
) {
    if (uiState.isLoading && uiState.paymentMethods.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MovoTeal)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 24.dp)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.wallet_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MovoOnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.wallet_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MovoOnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.paymentMethods.isEmpty()) {
                item {
                    EmptyWalletState()
                }
            } else {
                items(
                    uiState.paymentMethods,
                    key = { it.id }
                ) { method ->
                    PaymentMethodCard(
                        method = method,
                        onSetDefault = { onSetDefault(method.id) },
                        onDelete = { onDelete(method.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onViewTransactions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MovoTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.wallet_view_transactions),
                        color = MovoTeal,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MovoOnSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(Res.string.wallet_payments_secured),
                        style = MaterialTheme.typography.bodySmall,
                        color = MovoOnSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddCard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MovoTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.wallet_add_new_card),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun EmptyWalletState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MovoSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                tint = MovoOnSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.wallet_empty),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MovoOnSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.wallet_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MovoOnSurfaceVariant
        )
    }
}

@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, MovoOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardBrandIcon(brand = method.brand, type = method.type)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getMethodDisplayName(method),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MovoOnSurface
                )
                if (method.lastFour.isNotEmpty()) {
                    Text(
                        text = "•••• ${method.lastFour}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MovoOnSurfaceVariant
                    )
                }
                if (method.expiryMonth != null && method.expiryYear != null) {
                    Text(
                        text = stringResource(
                            Res.string.wallet_expires,
                            method.expiryMonth.toString().padStart(2, '0'),
                            method.expiryYear.toString().takeLast(2)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MovoOnSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onSetDefault) {
                if (method.isDefault) {
                    Icon(
                        imageVector = Icons.Default.RadioButtonChecked,
                        contentDescription = stringResource(Res.string.wallet_default),
                        tint = MovoTeal,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.RadioButtonUnchecked,
                        contentDescription = stringResource(Res.string.wallet_set_default),
                        tint = MovoOutline,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardBrandIcon(brand: String?, type: PaymentMethodType) {
    val (backgroundColor, iconColor) = when {
        brand?.lowercase() == "visa" -> Pair(Color(0xFF1A1F71), Color.White)
        brand?.lowercase() == "mastercard" -> Pair(Color(0xFFEB001B), Color.White)
        brand?.lowercase() == "amex" || brand?.lowercase() == "american express" -> Pair(
            Color(
                0xFF2E77BC
            ), Color.White
        )

        type == PaymentMethodType.PAYPAL -> Pair(Color(0xFF003087), Color.White)
        type == PaymentMethodType.APPLE_PAY -> Pair(Color(0xFF000000), Color.White)
        type == PaymentMethodType.GOOGLE_PAY -> Pair(Color(0xFF4285F4), Color.White)
        else -> Pair(MovoTeal, Color.White)
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when {
                brand?.lowercase() == "visa" -> "V"
                brand?.lowercase() == "mastercard" -> "M"
                brand?.lowercase() == "amex" || brand?.lowercase() == "american express" -> "A"
                type == PaymentMethodType.PAYPAL -> "P"
                type == PaymentMethodType.APPLE_PAY -> "A"
                type == PaymentMethodType.GOOGLE_PAY -> "G"
                else -> "•"
            },
            color = iconColor,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

private fun getMethodDisplayName(method: PaymentMethod): String {
    return when (method.type) {
        PaymentMethodType.CARD -> method.brand?.replaceFirstChar { it.uppercase() } ?: "Card"
        PaymentMethodType.PAYPAL -> "PayPal"
        PaymentMethodType.APPLE_PAY -> "Apple Pay"
        PaymentMethodType.GOOGLE_PAY -> "Google Pay"
    }
}

@Preview
@Composable
private fun WalletScreenPreview() {
    MovoTheme {
        WalletContent(
            uiState = WalletUiState(
                paymentMethods = listOf(
                    PaymentMethod(
                        id = "pm_1",
                        type = PaymentMethodType.CARD,
                        brand = "visa",
                        lastFour = "4242",
                        expiryMonth = 12,
                        expiryYear = 2027,
                        isDefault = true
                    ),
                    PaymentMethod(
                        id = "pm_2",
                        type = PaymentMethodType.CARD,
                        brand = "mastercard",
                        lastFour = "8888",
                        expiryMonth = 8,
                        expiryYear = 2026,
                        isDefault = false
                    ),
                    PaymentMethod(
                        id = "pm_3",
                        type = PaymentMethodType.PAYPAL,
                        lastFour = "",
                        isDefault = false
                    )
                ),
                isLoading = false,
                errorMessage = null,
                showAddCardDialog = false
            ),
            paddingValues = androidx.compose.foundation.layout.PaddingValues(),
            onSetDefault = {},
            onDelete = {},
            onViewTransactions = {},
            onAddCard = {}
        )
    }
}
