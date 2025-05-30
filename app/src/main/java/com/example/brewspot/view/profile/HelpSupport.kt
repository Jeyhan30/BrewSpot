package com.example.brewspot.view.profile


import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext // Keep this import
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(navController: NavController) {
    val brownColor = Color(0xFF5D4037) // Dark brown color
    val lightBrownBackground = Color(0xFFF0F0F0) // Light gray for overall background
    val context = LocalContext.current // <--- MOVED THIS LINE HERE

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Bantuan & Dukungan",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = brownColor,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(lightBrownBackground)
                .padding(horizontal = 24.dp), // Padding for the cards themselves
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp)) // Space below the top bar

            // Bantuan Telepon Card
            ContactOptionCard(
                icon = Icons.Default.Call, // Using Material Icons
                iconBackgroundColor = Color(0xFFE0BBE4), // Purple-ish background for icon
                title = "Bantuan Telepon",
                subtitle = "(021) 6510300",
                onClick = {
                    // val context = LocalContext.current // <--- REMOVED FROM HERE
                    val phoneNumber = "0216510300"
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phoneNumber")
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        // Handle case where no dialer app is available
                        // Toast.makeText(context, "No app to handle phone calls", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bantuan Email Card
            ContactOptionCard(
                icon = Icons.Default.Email, // Using Material Icons
                iconBackgroundColor = Color(0xFFA1E7E0), // Teal-ish background for icon
                title = "Bantuan Email",
                subtitle = "help@caffe.com",
                onClick = {
                    // val context = LocalContext.current // <--- REMOVED FROM HERE
                    val recipient = "help@caffe.com"
                    val subject = "Bantuan Aplikasi Brewspot"
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:") // Only email apps should handle this
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        // Handle case where no email app is available
                        // Toast.makeText(context, "No app to handle emails", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            // If you want more options, add them here
        }
    }
}

@Composable
fun ContactOptionCard(
    icon: ImageVector, // Or replace with painterResource if using custom drawables
    iconBackgroundColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp) // Fixed height for consistency
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with colored background
            Box(
                modifier = Modifier
                    .size(50.dp) // Size of the colored circle around the icon
                    .clip(RoundedCornerShape(12.dp)) // Rounded corners for the icon background
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null, // Content description for icon handled by text
                    tint = Color.White, // Icon color
                    modifier = Modifier.size(28.dp) // Size of the icon itself
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column {
                Text(
                    text = title,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}