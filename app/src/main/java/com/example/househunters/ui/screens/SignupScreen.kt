package com.example.househunters.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.househunters.ui.theme.HouseHuntersTheme

@Composable
fun SignupScreen(
    onCreateAccountClick: () -> Unit,
    onGotoLoginClick: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthGradientBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(120.dp))
            HouseHeader(title = "Signup")
            Spacer(modifier = Modifier.height(42.dp))

            AuthTextField(
                value = firstName,
                placeholder = "Enter First Name",
                onValueChange = { firstName = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(
                value = lastName,
                placeholder = "Enter Last Name",
                onValueChange = { lastName = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(
                value = email,
                placeholder = "Enter Email",
                onValueChange = { email = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(
                value = password,
                placeholder = "Enter Password",
                onValueChange = { password = it }
            )

            Spacer(modifier = Modifier.height(40.dp))
            AuthPrimaryButton(
                label = "Create Account",
                onClick = onCreateAccountClick,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            AuthPrimaryButton(
                label = "Go To Login",
                onClick = onGotoLoginClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    HouseHuntersTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            SignupScreen(
                onCreateAccountClick = {},
                onGotoLoginClick = {}
            )
        }
    }
}
