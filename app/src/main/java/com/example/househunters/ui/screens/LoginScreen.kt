package com.example.househunters.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginClick: (String, String) -> Unit,
    onGotoSignupClick: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    AuthGradientBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(120.dp))
            HouseHeader(title = "Login")
            Spacer(modifier = Modifier.height(46.dp))

            AuthTextField(
                value = email,
                placeholder = "Enter Email",
                onValueChange = { email = it }
            )
            Spacer(modifier = Modifier.height(18.dp))
            AuthTextField(
                value = password,
                placeholder = "Enter Password",
                onValueChange = { password = it }
            )
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            AuthPrimaryButton(
                label = if (isLoading) "Logging In..." else "Login",
                onClick = { onLoginClick(email.trim(), password) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            AuthPrimaryButton(
                label = "Go To Signup",
                onClick = onGotoSignupClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        isLoading = false,
        errorMessage = null,
        onLoginClick = { _, _ -> },
        onGotoSignupClick = {}
    )
}
