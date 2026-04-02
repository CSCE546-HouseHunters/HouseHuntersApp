package com.example.househunters.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit
) {
    AuthGradientBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(164.dp))
            HouseHeader(title = "", brandTitle = "HOUSE HUNTERS")
            Spacer(modifier = Modifier.height(132.dp))

            AuthPrimaryButton(
                label = "Login",
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(),
                height = 56.dp,
                shadowElevation = 14.dp,
                textSize = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            AuthPrimaryButton(
                label = "Sign Up",
                onClick = onSignupClick,
                modifier = Modifier.fillMaxWidth(),
                height = 56.dp,
                shadowElevation = 14.dp,
                textSize = 20.sp
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(
        onLoginClick = {},
        onSignupClick = {}
    )
}
