package it.movo.app.ui.subscription

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.movo.app.data.model.SubscriptionPlan
import it.movo.app.data.model.UserSubscription
import it.movo.app.ui.theme.MovoOnSurface
import it.movo.app.ui.theme.MovoOnSurfaceVariant
import it.movo.app.ui.theme.MovoOutline
import it.movo.app.ui.theme.MovoSurface
import it.movo.app.ui.theme.MovoSurfaceVariant
import it.movo.app.ui.theme.MovoTeal
import it.movo.app.ui.theme.MovoWhite
import it.movo.app.composeapp.generated.resources.Res
import it.movo.app.composeapp.generated.resources.back
import it.movo.app.composeapp.generated.resources.subscription_active
import it.movo.app.composeapp.generated.resources.subscription_auto_renew
import it.movo.app.composeapp.generated.resources.subscription_billing_period
import it.movo.app.composeapp.generated.resources.subscription_cancel
import it.movo.app.composeapp.generated.resources.subscription_discount
import it.movo.app.composeapp.generated.resources.subscription_features
import it.movo.app.composeapp.generated.resources.subscription_minutes_included
import it.movo.app.composeapp.generated.resources.subscription_minutes_remaining
import it.movo.app.composeapp.generated.resources.subscription_minutes_used
import it.movo.app.composeapp.generated.resources.subscription_no_active
import it.movo.app.composeapp.generated.resources.subscription_plans
import it.movo.app.composeapp.generated.resources.subscription_price_per_month
import it.movo.app.composeapp.generated.resources.subscription_subscribe
import it.movo.app.composeapp.generated.resources.subscription_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.subscribeSuccess) {
        if (uiState.subscribeSuccess) {
            viewModel.clearSubscribeSuccess()
        }
    }

    LaunchedEffect(uiState.cancelSuccess) {
        if (uiState.cancelSuccess) {
            viewModel.clearCancelSuccess()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0.dp),
                title = {
                    Text(
                        text = stringResource(Res.string.subscription_title),
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
        }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.plans.isEmpty() && uiState.activeSubscription == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MovoTeal)
                }
            }

            uiState.errorMessage != null && uiState.plans.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MovoOnSurface
                        )
                        Button(
                            onClick = { viewModel.loadPlans() },
                            colors = ButtonDefaults.buttonColors(containerColor = MovoTeal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(Res.string.subscription_subscribe))
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.subscription_active),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MovoOnSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    val activeSubscription = uiState.activeSubscription
                    if (activeSubscription != null) {
                        item {
                            ActiveSubscriptionCard(
                                subscription = activeSubscription,
                                onToggleAutoRenew = { viewModel.toggleAutoRenew() },
                                onCancel = { viewModel.cancelSubscription() }
                            )
                        }
                    } else {
                        item {
                            NoActiveSubscriptionCard()
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(Res.string.subscription_plans),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MovoOnSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(
                        items = uiState.plans,
                        key = { it.id }
                    ) { plan ->
                        SubscriptionPlanCard(
                            plan = plan,
                            onSubscribe = { viewModel.subscribe(plan.id) },
                            isSubscribing = uiState.isSubscribing
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveSubscriptionCard(
    subscription: UserSubscription,
    onToggleAutoRenew: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoWhite),
        border = BorderStroke(1.dp, MovoOutline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subscription.plan?.name ?: subscription.planId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MovoOnSurface
                )
                Text(
                    text = subscription.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MovoTeal,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (subscription.plan?.billingPeriod != null) {
                Text(
                    text = stringResource(
                        Res.string.subscription_billing_period,
                        subscription.plan.billingPeriod
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MovoOnSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.subscription_auto_renew),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MovoOnSurface
                )
                Switch(
                    checked = subscription.autoRenew,
                    onCheckedChange = { onToggleAutoRenew() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MovoWhite,
                        checkedTrackColor = MovoTeal,
                        uncheckedThumbColor = MovoWhite,
                        uncheckedTrackColor = MovoSurfaceVariant
                    )
                )
            }

            if (subscription.minutesUsed > 0 || subscription.minutesRemaining != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(
                            Res.string.subscription_minutes_used,
                            subscription.minutesUsed
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MovoOnSurfaceVariant
                    )
                    subscription.minutesRemaining?.let { remaining ->
                        Text(
                            text = stringResource(
                                Res.string.subscription_minutes_remaining,
                                remaining
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MovoTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MovoSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.subscription_cancel),
                    color = MovoOnSurface
                )
            }
        }
    }
}

@Composable
private fun NoActiveSubscriptionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoSurfaceVariant),
        border = BorderStroke(1.dp, MovoOutline)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.subscription_no_active),
                style = MaterialTheme.typography.bodyLarge,
                color = MovoOnSurfaceVariant
            )
        }
    }
}

@Composable
private fun SubscriptionPlanCard(
    plan: SubscriptionPlan,
    onSubscribe: () -> Unit,
    isSubscribing: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoWhite),
        border = BorderStroke(1.dp, MovoOutline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MovoOnSurface
            )

            if (plan.description != null) {
                Text(
                    text = plan.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MovoOnSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        Res.string.subscription_price_per_month,
                        formatPrice(plan.priceCents)
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MovoTeal
                )

                if (plan.discountPercentage > 0) {
                    Text(
                        text = stringResource(
                            Res.string.subscription_discount,
                            plan.discountPercentage
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MovoTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (plan.includedMinutes != null) {
                Text(
                    text = stringResource(
                        Res.string.subscription_minutes_included,
                        plan.includedMinutes
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MovoOnSurface
                )
            }

            if (plan.features.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.subscription_features),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MovoOnSurface
                )
                plan.features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MovoTeal,
                            modifier = Modifier.width(16.dp)
                        )
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MovoOnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onSubscribe,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubscribing,
                colors = ButtonDefaults.buttonColors(containerColor = MovoTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubscribing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MovoWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(Res.string.subscription_subscribe))
                }
            }
        }
    }
}

private fun formatPrice(cents: Int): String {
    val euros = cents / 100
    val remainingCents = cents % 100
    return "€$euros.${remainingCents.toString().padStart(2, '0')}"
}
