package com.example.househunters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HeaderMint = Color(0xFF83D9D2)
private val FieldShadow = Color(0x3383D9D2)
private val ButtonShadow = Color(0x4D6BB8B1)

@Composable
fun AuthGradientBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFE2F1EF),
                        Color(0xFFA0EAE1)
                    )
                )
            )
            .padding(horizontal = 24.dp)
    ) {
        content()
    }
}

@Composable
fun HouseHeader(title: String, brandTitle: String? = null) {
    if (brandTitle != null) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    tint = HeaderMint,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = brandTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF202020),
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp
                )
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = null,
                tint = HeaderMint,
                modifier = Modifier.size(84.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = HeaderMint,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFF7E8A8C)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF9FEFD),
            unfocusedContainerColor = Color(0xFFF9FEFD),
            focusedBorderColor = HeaderMint,
            unfocusedBorderColor = Color(0x80A7CFCA)
        ),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = FieldShadow,
                spotColor = FieldShadow
            )
    )
}

@Composable
fun AuthPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 50.dp,
    shadowElevation: Dp = 10.dp,
    textSize: TextUnit = 16.sp
) {
    Box(
        modifier = modifier
            .height(height)
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(30.dp),
                ambientColor = ButtonShadow,
                spotColor = ButtonShadow
            )
    ) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(30.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HeaderMint,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                fontSize = textSize
            )
        }
    }
}
