package it.movo.app.ui.issue

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.movo.app.data.model.IssueCategory
import it.movo.app.ui.theme.MovoOnSurface
import it.movo.app.ui.theme.MovoOnSurfaceVariant
import it.movo.app.ui.theme.MovoOutline
import it.movo.app.ui.theme.MovoSuccess
import it.movo.app.ui.theme.MovoSurface
import it.movo.app.ui.theme.MovoSurfaceVariant
import it.movo.app.ui.theme.MovoTeal
import it.movo.app.ui.theme.MovoTheme
import it.movo.app.composeapp.generated.resources.Res
import it.movo.app.composeapp.generated.resources.back
import it.movo.app.composeapp.generated.resources.common_done
import it.movo.app.composeapp.generated.resources.report_add_photo
import it.movo.app.composeapp.generated.resources.report_category
import it.movo.app.composeapp.generated.resources.report_category_battery
import it.movo.app.composeapp.generated.resources.report_category_cleanliness
import it.movo.app.composeapp.generated.resources.report_category_damage
import it.movo.app.composeapp.generated.resources.report_category_other
import it.movo.app.composeapp.generated.resources.report_category_technical
import it.movo.app.composeapp.generated.resources.report_description
import it.movo.app.composeapp.generated.resources.report_description_hint
import it.movo.app.composeapp.generated.resources.report_max_photos
import it.movo.app.composeapp.generated.resources.report_photos
import it.movo.app.composeapp.generated.resources.report_submit
import it.movo.app.composeapp.generated.resources.report_success
import it.movo.app.composeapp.generated.resources.report_title
import it.movo.app.composeapp.generated.resources.ride_report_issue
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIssueScreen(
    viewModel: ReportIssueViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0.dp),
                title = {
                    Text(
                        text = stringResource(Res.string.report_title),
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
        ReportIssueContent(
            uiState = uiState,
            paddingValues = paddingValues,
            onNavigateBack = onNavigateBack,
            onToggleCategoryMenu = { viewModel.toggleCategoryMenu() },
            onDismissCategoryMenu = { viewModel.dismissCategoryMenu() },
            onCategorySelected = { viewModel.onCategorySelected(it) },
            onDescriptionChange = { viewModel.onDescriptionChange(it) },
            onAddPhoto = { viewModel.pickAndAddPhoto() },
            onSubmitReport = { viewModel.submitReport() }
        )
    }
}

@Composable
private fun ReportIssueContent(
    uiState: ReportIssueUiState,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    onNavigateBack: () -> Unit,
    onToggleCategoryMenu: () -> Unit,
    onDismissCategoryMenu: () -> Unit,
    onCategorySelected: (IssueCategory) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddPhoto: () -> Unit,
    onSubmitReport: () -> Unit
) {
    if (uiState.isSubmitted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MovoSuccess,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.report_success),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onNavigateBack) {
                    Text(
                        text = stringResource(Res.string.common_done),
                        color = MovoTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(Res.string.report_category),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MovoOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            CategoryDropdown(
                selectedCategory = uiState.selectedCategory,
                expanded = uiState.isCategoryMenuExpanded,
                onToggle = onToggleCategoryMenu,
                onDismiss = onDismissCategoryMenu,
                onSelect = onCategorySelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(Res.string.report_description),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MovoOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = { Text(stringResource(Res.string.report_description_hint)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MovoTeal,
                    focusedLabelColor = MovoTeal
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(Res.string.report_photos),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MovoOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            PhotoPlaceholders(
                photoCount = uiState.photos.size,
                onAddPhoto = onAddPhoto
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(Res.string.report_max_photos),
                style = MaterialTheme.typography.bodySmall,
                color = MovoOnSurfaceVariant
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onSubmitReport,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MovoTeal),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.report_submit),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CategoryDropdown(
    selectedCategory: IssueCategory?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (IssueCategory) -> Unit
) {
    val categories = listOf(
        IssueCategory.TECHNICAL to Res.string.report_category_technical,
        IssueCategory.DAMAGE to Res.string.report_category_damage,
        IssueCategory.BATTERY to Res.string.report_category_battery,
        IssueCategory.CLEANLINESS to Res.string.report_category_cleanliness,
        IssueCategory.OTHER to Res.string.report_category_other
    )

    val selectedLabel = when (selectedCategory) {
        IssueCategory.TECHNICAL -> stringResource(Res.string.report_category_technical)
        IssueCategory.DAMAGE -> stringResource(Res.string.report_category_damage)
        IssueCategory.BATTERY -> stringResource(Res.string.report_category_battery)
        IssueCategory.CLEANLINESS -> stringResource(Res.string.report_category_cleanliness)
        IssueCategory.OTHER -> stringResource(Res.string.report_category_other)
        null -> stringResource(Res.string.report_category)
    }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MovoSurfaceVariant),
            border = BorderStroke(1.dp, MovoOutline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedCategory != null) MovoOnSurface else MovoOnSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MovoOnSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss
        ) {
            categories.forEach { (category, stringRes) ->
                DropdownMenuItem(
                    text = { Text(stringResource(stringRes)) },
                    onClick = { onSelect(category) }
                )
            }
        }
    }
}

@Composable
private fun PhotoPlaceholders(
    photoCount: Int,
    onAddPhoto: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(5) { index ->
            val hasPhoto = index < photoCount
            Card(
                modifier = Modifier
                    .size(80.dp)
                    .clickable(enabled = !hasPhoto && photoCount < 5) { onAddPhoto() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasPhoto) MovoTeal.copy(alpha = 0.1f) else MovoSurfaceVariant
                ),
                border = BorderStroke(1.dp, if (hasPhoto) MovoTeal else MovoOutline)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (hasPhoto) Icons.Default.CheckCircle else Icons.Default.AddAPhoto,
                        contentDescription = stringResource(Res.string.report_add_photo),
                        tint = if (hasPhoto) MovoTeal else MovoOnSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ReportIssueScreenPreview() {
    val mockUiState = ReportIssueUiState(
        vehicleId = "VEH123",
        selectedCategory = IssueCategory.TECHNICAL,
        description = "The scooter's brake seems to be not working properly. It makes a squeaking sound when applied.",
        photos = emptyList(),
        isCategoryMenuExpanded = false,
        isLoading = false,
        isSubmitted = false,
        errorMessage = null
    )

    MovoTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0.dp),
                    title = {
                        Text(
                            text = stringResource(Res.string.ride_report_issue),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
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
            ReportIssueContent(
                uiState = mockUiState,
                paddingValues = paddingValues,
                onNavigateBack = {},
                onToggleCategoryMenu = {},
                onDismissCategoryMenu = {},
                onCategorySelected = {},
                onDescriptionChange = {},
                onAddPhoto = {},
                onSubmitReport = {}
            )
        }
    }
}