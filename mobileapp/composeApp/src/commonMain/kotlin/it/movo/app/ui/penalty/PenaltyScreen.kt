package it.movo.app.ui.penalty

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.movo.app.data.model.Penalty
import it.movo.app.data.model.PenaltyStatus
import it.movo.app.data.model.PenaltyType
import it.movo.app.ui.theme.MovoOnSurface
import it.movo.app.ui.theme.MovoOnSurfaceVariant
import it.movo.app.ui.theme.MovoOutline
import it.movo.app.ui.theme.MovoSuccess
import it.movo.app.ui.theme.MovoSurface
import it.movo.app.ui.theme.MovoTeal
import it.movo.app.ui.theme.MovoWarning
import it.movo.app.ui.theme.MovoWhite
import it.movo.app.composeapp.generated.resources.Res
import it.movo.app.composeapp.generated.resources.back
import it.movo.app.composeapp.generated.resources.ok
import it.movo.app.composeapp.generated.resources.penalty_amount
import it.movo.app.composeapp.generated.resources.penalty_cancel
import it.movo.app.composeapp.generated.resources.penalty_contest
import it.movo.app.composeapp.generated.resources.penalty_contest_hint
import it.movo.app.composeapp.generated.resources.penalty_contest_reason
import it.movo.app.composeapp.generated.resources.penalty_contest_submit
import it.movo.app.composeapp.generated.resources.penalty_contest_success
import it.movo.app.composeapp.generated.resources.penalty_due_date
import it.movo.app.composeapp.generated.resources.penalty_no_penalties
import it.movo.app.composeapp.generated.resources.penalty_retry
import it.movo.app.composeapp.generated.resources.penalty_status_cancelled
import it.movo.app.composeapp.generated.resources.penalty_status_contested
import it.movo.app.composeapp.generated.resources.penalty_status_paid
import it.movo.app.composeapp.generated.resources.penalty_status_pending
import it.movo.app.composeapp.generated.resources.penalty_success
import it.movo.app.composeapp.generated.resources.penalty_title
import it.movo.app.composeapp.generated.resources.penalty_type_damage
import it.movo.app.composeapp.generated.resources.penalty_type_geofence_exit
import it.movo.app.composeapp.generated.resources.penalty_type_late
import it.movo.app.composeapp.generated.resources.penalty_type_other
import it.movo.app.composeapp.generated.resources.penalty_type_parking
import it.movo.app.composeapp.generated.resources.penalty_type_traffic
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenaltyScreen(
    viewModel: PenaltyViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.penalty_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MovoSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        color = MovoTeal,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.errorMessage != null && uiState.penalties.isEmpty() -> {
                    ErrorState(
                        message = uiState.errorMessage!!,
                        onRetry = { viewModel.loadPenalties() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.penalties.isEmpty() -> {
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    PenaltyList(
                        penalties = uiState.penalties,
                        onContestClick = { viewModel.loadPenalty(it.id) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    uiState.selectedPenalty?.let { penalty ->
        ContestDialog(
            penalty = penalty,
            contestReason = uiState.contestReason,
            isContesting = uiState.isContesting,
            errorMessage = uiState.errorMessage,
            onReasonChange = { viewModel.onContestReasonChange(it) },
            onSubmit = { viewModel.contestPenalty(penalty.id) },
            onDismiss = { viewModel.clearSelectedPenalty() }
        )
    }

    if (uiState.contestSuccess) {
        SuccessDialog(
            onDismiss = {
                viewModel.clearContestSuccess()
                viewModel.clearSelectedPenalty()
            }
        )
    }
}

@Composable
private fun PenaltyList(
    penalties: List<Penalty>,
    onContestClick: (Penalty) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(penalties) { penalty ->
            PenaltyCard(
                penalty = penalty,
                onContestClick = { onContestClick(penalty) }
            )
        }
    }
}

@Composable
private fun PenaltyCard(
    penalty: Penalty,
    onContestClick: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TypeChip(type = penalty.type)
                StatusChip(status = penalty.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(
                    Res.string.penalty_amount,
                    penalty.amountCents / 100
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MovoOnSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = penalty.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MovoOnSurfaceVariant
            )

            penalty.dueDate?.let { dueDate ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.penalty_due_date, dueDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MovoOnSurfaceVariant
                )
            }

            if (penalty.status == PenaltyStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onContestClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MovoTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.penalty_contest),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeChip(type: PenaltyType) {
    val label = when (type) {
        PenaltyType.PARKING_VIOLATION -> stringResource(Res.string.penalty_type_parking)
        PenaltyType.GEOFENCE_EXIT -> stringResource(Res.string.penalty_type_geofence_exit)
        PenaltyType.DAMAGE -> stringResource(Res.string.penalty_type_damage)
        PenaltyType.TRAFFIC_FINE -> stringResource(Res.string.penalty_type_traffic)
        PenaltyType.LATE_RETURN -> stringResource(Res.string.penalty_type_late)
        PenaltyType.OTHER -> stringResource(Res.string.penalty_type_other)
    }

    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MovoSurface,
            labelColor = MovoOnSurfaceVariant
        )
    )
}

@Composable
private fun StatusChip(status: PenaltyStatus) {
    val (label, color) = when (status) {
        PenaltyStatus.PENDING -> stringResource(Res.string.penalty_status_pending) to MovoWarning
        PenaltyStatus.PAID -> stringResource(Res.string.penalty_status_paid) to MovoSuccess
        PenaltyStatus.CONTESTED -> stringResource(Res.string.penalty_status_contested) to MovoTeal
        PenaltyStatus.CANCELLED -> stringResource(Res.string.penalty_status_cancelled) to MovoOnSurfaceVariant
    }

    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.1f),
            labelColor = color
        )
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.penalty_no_penalties),
            style = MaterialTheme.typography.bodyLarge,
            color = MovoOnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MovoTeal)
        ) {
            Text(stringResource(Res.string.penalty_retry))
        }
    }
}

@Composable
private fun ContestDialog(
    penalty: Penalty,
    contestReason: String,
    isContesting: Boolean,
    errorMessage: String?,
    onReasonChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.penalty_contest),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(Res.string.penalty_contest_reason),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MovoOnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contestReason,
                    onValueChange = onReasonChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(Res.string.penalty_contest_hint)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MovoTeal,
                        focusedLabelColor = MovoTeal
                    ),
                    minLines = 3,
                    maxLines = 5
                )
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !isContesting && contestReason.length >= 20,
                colors = ButtonDefaults.buttonColors(containerColor = MovoTeal)
            ) {
                if (isContesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(Res.string.penalty_contest_submit))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.penalty_cancel), color = MovoTeal)
            }
        }
    )
}

@Composable
private fun SuccessDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.penalty_success),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.penalty_contest_success),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MovoTeal)
            ) {
                Text(stringResource(Res.string.ok))
            }
        }
    )
}
