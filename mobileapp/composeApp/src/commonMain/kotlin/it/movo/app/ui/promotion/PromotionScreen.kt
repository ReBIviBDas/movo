package it.movo.app.ui.promotion

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.movo.app.data.model.Promotion
import it.movo.app.data.model.PromotionType
import it.movo.app.ui.theme.MovoOnSurface
import it.movo.app.ui.theme.MovoOnSurfaceVariant
import it.movo.app.ui.theme.MovoOutline
import it.movo.app.ui.theme.MovoSuccess
import it.movo.app.ui.theme.MovoSurface
import it.movo.app.ui.theme.MovoTeal
import it.movo.app.ui.theme.MovoWhite
import it.movo.app.composeapp.generated.resources.Res
import it.movo.app.composeapp.generated.resources.back
import it.movo.app.composeapp.generated.resources.promotion_apply
import it.movo.app.composeapp.generated.resources.promotion_available
import it.movo.app.composeapp.generated.resources.promotion_code_applied
import it.movo.app.composeapp.generated.resources.promotion_discount
import it.movo.app.composeapp.generated.resources.promotion_enter_code
import it.movo.app.composeapp.generated.resources.promotion_no_promotions
import it.movo.app.composeapp.generated.resources.promotion_title
import it.movo.app.composeapp.generated.resources.promotion_type
import it.movo.app.composeapp.generated.resources.promotion_valid_until
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionScreen(
    viewModel: PromotionViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val appliedPromoMessage = uiState.appliedPromotion?.let { promo ->
        "${stringResource(Res.string.promotion_code_applied)}: ${promo.code}"
    }

    LaunchedEffect(uiState.appliedPromotion) {
        appliedPromoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearAppliedPromotion()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.promotion_title),
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
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.promotions.isEmpty()) {
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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    PromoCodeInputSection(
                        promoCode = uiState.promoCode,
                        isApplying = uiState.isApplying,
                        onPromoCodeChange = { viewModel.onPromoCodeChange(it) },
                        onApply = { viewModel.applyPromoCode() }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.promotion_available),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MovoOnSurface
                    )
                }

                if (uiState.promotions.isEmpty()) {
                    item {
                        EmptyPromotionsState()
                    }
                } else {
                    items(
                        uiState.promotions,
                        key = { it.id }
                    ) { promotion ->
                        PromotionCard(promotion = promotion)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PromoCodeInputSection(
    promoCode: String,
    isApplying: Boolean,
    onPromoCodeChange: (String) -> Unit,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoWhite),
        border = BorderStroke(1.dp, MovoOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = promoCode,
                onValueChange = onPromoCodeChange,
                label = { Text(stringResource(Res.string.promotion_enter_code)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MovoTeal,
                    focusedLabelColor = MovoTeal
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MovoTeal),
                shape = RoundedCornerShape(12.dp),
                enabled = !isApplying && promoCode.isNotBlank()
            ) {
                if (isApplying) {
                    CircularProgressIndicator(
                        color = MovoWhite,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.promotion_apply),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPromotionsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalOffer,
                contentDescription = null,
                tint = MovoOnSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.promotion_no_promotions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MovoOnSurface
        )
    }
}

@Composable
private fun PromotionCard(promotion: Promotion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoWhite),
        border = BorderStroke(1.dp, MovoOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = MovoTeal,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = promotion.code.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MovoOnSurface
                    )

                    if (promotion.description != null) {
                        Text(
                            text = promotion.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MovoOnSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(start = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MovoSuccess,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.promotion_type),
                        style = MaterialTheme.typography.labelSmall,
                        color = MovoOnSurfaceVariant
                    )
                    Text(
                        text = promotion.type.name.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MovoOnSurface
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = stringResource(Res.string.promotion_discount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MovoOnSurfaceVariant
                    )
                    Text(
                        text = if (promotion.type == PromotionType.PERCENTAGE) {
                            "${promotion.value.toInt()}%"
                        } else {
                            "€${promotion.value}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MovoTeal
                    )
                }
            }

            if (promotion.validUntil != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.promotion_valid_until, promotion.validUntil),
                    style = MaterialTheme.typography.bodySmall,
                    color = MovoOnSurfaceVariant
                )
            }
        }
    }
}
