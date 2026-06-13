package com.example.habits.ui.habit

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habits.R
import com.example.habits.data.util.DateFormatter
import com.example.habits.domain.model.Frequency
import com.example.habits.domain.model.UiState
import com.example.habits.ui.components.LoadingOverlay
import com.example.habits.ui.theme.DeleteActionContainer
import com.example.habits.ui.theme.DeleteActionContainerDark
import com.example.habits.ui.theme.DeleteActionContent
import com.example.habits.ui.theme.DeleteActionContentDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    viewModel: HabitDetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val checkIns by viewModel.checkIns.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val isLoading = uiState.loadState is UiState.Loading || uiState.deleteState is UiState.Loading
    val habit = uiState.habit

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.habit_detail_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_content_description),
                        )
                    }
                },
                actions = {
                    if (habit != null) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_habit_content_description),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            uiState.loadState is UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                ) {
                    Text(
                        text = (uiState.loadState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            habit != null -> {
                val isDarkTheme = isSystemInDarkTheme()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = habit.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = habit.description.ifBlank {
                                        stringResource(R.string.no_description)
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when (habit.frequency) {
                                        Frequency.DAILY -> stringResource(R.string.frequency_daily)
                                        Frequency.WEEKLY -> stringResource(R.string.frequency_weekly)
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                    item {
                        Text(
                            text = stringResource(R.string.history_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    if (checkIns.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.empty_history_message),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                    } else {
                        items(checkIns, key = { it.id }) { checkIn ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = DateFormatter.format(checkIn.completedAt),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(16.dp),
                                )
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDarkTheme) {
                                    DeleteActionContainerDark
                                } else {
                                    DeleteActionContainer
                                },
                                contentColor = if (isDarkTheme) {
                                    DeleteActionContentDark
                                } else {
                                    DeleteActionContent
                                },
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .heightIn(min = 48.dp),
                        ) {
                            Text(stringResource(R.string.delete_habit_button))
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_habit_title)) },
            text = { Text(stringResource(R.string.delete_habit_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteHabit(onDeleted)
                    },
                ) {
                    Text(
                        text = stringResource(R.string.delete_confirm),
                        color = if (isSystemInDarkTheme()) {
                            DeleteActionContentDark
                        } else {
                            DeleteActionContent
                        },
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    LoadingOverlay(isVisible = isLoading)
}
