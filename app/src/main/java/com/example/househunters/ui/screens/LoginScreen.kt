package com.example.househunters.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun LoginScreen(
    onLoginClick: () -> Unit,
    onGotoSignupClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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

            Spacer(modifier = Modifier.height(28.dp))
            AuthPrimaryButton(
                label = "Login",
                onClick = onLoginClick,
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
            onLoginClick = {},
            onGotoSignupClick = {}
        )

}
