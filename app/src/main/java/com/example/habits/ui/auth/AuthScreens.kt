package com.example.habits.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habits.R
import com.example.habits.domain.model.UiState
import com.example.habits.ui.components.AppTextField
import com.example.habits.ui.components.LoadingOverlay

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = uiState.submitState is UiState.Loading

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            )

            AppTextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChange,
                label = stringResource(R.string.username_label),
                isError = uiState.usernameError != null,
                errorMessage = uiState.usernameError,
            )
            Spacer(modifier = Modifier.height(12.dp))
            AppTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = stringResource(R.string.password_label),
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.login(onLoginSuccess) },
                enabled = !isLoading,
                modifier = Modifier
                    .heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.login_button))
            }
            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.no_account))
            }
        }
    }

    LoadingOverlay(isVisible = isLoading)
}

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = uiState.submitState is UiState.Loading

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            )

            AppTextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChange,
                label = stringResource(R.string.username_label),
                isError = uiState.usernameError != null,
                errorMessage = uiState.usernameError,
            )
            Spacer(modifier = Modifier.height(12.dp))
            AppTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = stringResource(R.string.password_label),
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            AppTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = stringResource(R.string.confirm_password_label),
                isError = uiState.confirmPasswordError != null,
                errorMessage = uiState.confirmPasswordError,
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.register(onRegisterSuccess) },
                enabled = !isLoading,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.register_button))
            }
            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.have_account))
            }
        }
    }

    LoadingOverlay(isVisible = isLoading)
}
