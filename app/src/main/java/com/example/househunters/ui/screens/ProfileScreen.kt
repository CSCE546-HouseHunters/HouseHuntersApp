package com.example.househunters.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.househunters.data.remote.UserProfileResponse
import com.example.househunters.ui.components.NavBar
import com.example.househunters.ui.navigation.Screen
import com.example.househunters.ui.viewmodel.ProfileUiState

@Composable
fun ProfileScreen(
    user: UserProfileResponse?,
    profileState: ProfileUiState,
    onSaveProfile: (String, String, String, String) -> Unit,
    onClearMessages: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    var firstName by rememberSaveable(user?.userId) { mutableStateOf(user?.firstName.orEmpty()) }
    var lastName by rememberSaveable(user?.userId) { mutableStateOf(user?.lastName.orEmpty()) }
    var email by rememberSaveable(user?.userId) { mutableStateOf(user?.email.orEmpty()) }
    var phone by rememberSaveable(user?.userId) { mutableStateOf(user?.phone.orEmpty()) }

    LaunchedEffect(user?.userId, user?.firstName, user?.lastName, user?.email, user?.phone) {
        firstName = user?.firstName.orEmpty()
        lastName = user?.lastName.orEmpty()
        email = user?.email.orEmpty()
        phone = user?.phone.orEmpty()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .padding(bottom = 120.dp)
        ) {
            Text(
                text = "Your profile",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user?.let { "Manage your House Hunters account." }
                    ?: "Log in to manage your House Hunters account.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Account info",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = user?.let { "User ID: ${it.userId}" } ?: "User ID unavailable",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = user?.let { "Logged in as ${it.email}" } ?: "You are not logged in.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    firstName = it
                    onClearMessages()
                },
                label = { Text("First name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                enabled = user != null && !profileState.isSaving
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    lastName = it
                    onClearMessages()
                },
                label = { Text("Last name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                enabled = user != null && !profileState.isSaving
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    onClearMessages()
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = user != null && !profileState.isSaving
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    onClearMessages()
                },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = user != null && !profileState.isSaving
            )

            profileState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            profileState.successMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AuthPrimaryButton(
                    label = "Cancel",
                    onClick = {
                        firstName = user?.firstName.orEmpty()
                        lastName = user?.lastName.orEmpty()
                        email = user?.email.orEmpty()
                        phone = user?.phone.orEmpty()
                        onClearMessages()
                    },
                    enabled = user != null && !profileState.isSaving,
                    modifier = Modifier.weight(1f)
                )
                AuthPrimaryButton(
                    label = if (profileState.isSaving) "Saving..." else "Save changes",
                    onClick = {
                        onSaveProfile(
                            firstName.trim(),
                            lastName.trim(),
                            email.trim(),
                            phone.trim()
                        )
                    },
                    enabled = user != null && !profileState.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }

            if (profileState.isSaving) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        NavBar(
            currentRoute = Screen.Profile,
            onNavigate = onNavigate,
            onLogout = onLogout,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
