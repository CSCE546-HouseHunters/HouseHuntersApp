package com.example.househunters.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.househunters.ui.components.NavBar
import com.example.househunters.ui.theme.HouseHuntersTheme

data class ListingDetails(
    val id: String,
    val address: String,
    val imageResIds: List<Int>
)

@Composable
fun Listing(
    listing: ListingDetails,
    onBackClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // Horizontal scrollable photos
                LazyRow(modifier = Modifier.fillMaxSize()) {
                    items(listing.imageResIds) { imageResId ->
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .height(300.dp)
                        )
                    }
                }

                // Back button overlaid in top-left
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(40.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.7f)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                }
            }

            // Address beneath photos
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = listing.address,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Add more template fields here for the user to edit later
                Text(
                    text = "Additional details can be added here...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Add enough space at the bottom for the fixed overlay
                Spacer(modifier = Modifier.height(200.dp))
            }
        }

        // Overlay with Reserve button and NavBar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthPrimaryButton(
                label = "Reserve",
                onClick = { /* Functionality to be added later */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            NavBar(
                currentRoute = null, // Since we are in a sub-screen
                onNavigate = { /* Navigation logic */ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListingPreview() {
    val sampleListing = ListingDetails(
        id = "1",
        address = "123 Dream Lane, House City, HC 56789",
        imageResIds = listOf(
            android.R.drawable.ic_menu_gallery,
            android.R.drawable.ic_menu_camera,
            android.R.drawable.ic_menu_share
        )
    )
    HouseHuntersTheme {
        Listing(
            listing = sampleListing,
            onBackClick = {}
        )
    }
}
