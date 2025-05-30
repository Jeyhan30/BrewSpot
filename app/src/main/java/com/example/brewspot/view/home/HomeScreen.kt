package com.example.brewspot.view.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.example.brewspot.R
import com.example.brewspot.view.home.Cafe
import com.example.brewspot.view.home.HomeViewModel


class CustomTopShape : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = androidx.compose.ui.graphics.Outline.Generic(Path().apply {
        val width = size.width
        val height = size.height

        // Define the points for the curve
        // The curve starts at a certain percentage down the left side,
        // goes down, and then comes back up to the same percentage on the right side.
        // The control point will be below these start/end points.

        val curveStartEndY = height * 0.8f // Start and end the curve at 75% of the height
        val curveControlY = height * 1.2f // The control point reaches the full height (or slightly beyond if you want a deeper dip)

        moveTo(0f, 0f) // Start at top-left
        lineTo(width, 0f) // Line to top-right
        lineTo(width, curveStartEndY) // Line down to the right start point of the curve
        quadraticBezierTo(
            width / 2, curveControlY, // Control point for the dip (center-bottom of the shape)
            0f, curveStartEndY // End point at the left side, mirroring the right start point
        )
        close()
    })
}


fun decodeBase64ToBitmap(base64Str: String?): Bitmap? {
    if (base64Str.isNullOrEmpty()) {
        return null
    }
    // Check if the string contains the "data:image" prefix
    val cleanBase64Str = if (base64Str.startsWith("data:image", ignoreCase = true)) {
        base64Str.substringAfter(",") // Extract the actual Base64 data after the comma
    } else {
        base64Str // If no prefix, use the string as is
    }
    return try {
        val decodedBytes = Base64.decode(cleanBase64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        // Log the error for debugging purposes
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val recommendedCafes by viewModel.recommendedCafes.collectAsState()
    val popularCafes by viewModel.popularCafes.collectAsState()

    val brownColor = Color(0xFF5D4037)

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .background(Color(0xFFF0F0F0))
        ) {
            // Top Section - Brown Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // Adjust height to accommodate the curve
                    // If your curve goes slightly below the original height,
                    // you need to increase this height.
                    // Let's try 280.dp to give some room for the dip if needed.
                    .height(280.dp) // Slightly increased height to ensure curve is visible
                    .clip(CustomTopShape())
                    .background(brownColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { /* TODO: Lokasi picker */ }
                        ) {
                            Text(
                                "Pilih Lokasi",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Kota Malang",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.down),
                                contentDescription = "Pilih Lokasi",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = { /* TODO: Notifikasi */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.notification),
                                contentDescription = "Notifikasi",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Hi, Brews \uD83D\uDC4B",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Sudah ngafe belum hari ini?",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = "",
                        onValueChange = { /* TODO: Update search query */ },
                        placeholder = { Text("Cari", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = "Search Icon",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(horizontal = 0.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedLabelColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            }

            // Categories Section
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                Text(
                    "Kategori",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryButton("Terdekat") {}
                    CategoryButton("Estetik") {}
                    CategoryButton("Murah") {}
                }
            }

            // Rekomendasi Kafe Section (Horizontal)
            Column(modifier = Modifier.padding(bottom = 20.dp)) {
                Text(
                    "Rekomendasi Kafe",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recommendedCafes) { cafe ->
                        CafeCardHorizontal(cafe = cafe) {
                            navController.navigate("cafeDetail/${cafe.id}")

                        }
                    }
                }
            }

// Kafe Paling Populer Section (Vertical)
            Column(modifier = Modifier.padding(bottom = 20.dp)) {
                Text(
                    "Kafe Paling Populer",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp) // Apply horizontal padding to the title
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow( // THIS IS THE KEY CHANGE for side-by-side arrangement
                    contentPadding = PaddingValues(horizontal = 24.dp), // Padding around the entire row of items
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Spacing BETWEEN each cafe card
                ) {
                    items(popularCafes) { cafe ->
                        CafeCardVertical(cafe = cafe) { // Your individual card composable
                            navController.navigate("cafeDetail/${cafe.id}")                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text, color = Color.White, fontSize = 14.sp)
    }
}// Tambahkan ini di bagian bawah kode setelah CafeCardHorizontal dan CategoryButton
@Composable
fun CafeCardHorizontal(cafe: Cafe, onClick: () -> Unit) {
    val imageModel: Any = remember(cafe.image) {
        if (cafe.image.isNullOrEmpty()) {
            R.drawable.cafeeee // Jika string gambar kosong, gunakan placeholder
        } else if (cafe.image.startsWith("http://") || cafe.image.startsWith("https://")) {
            cafe.image // Jika ini adalah URL, biarkan Coil menanganinya langsung sebagai String
        } else {
            // Jika bukan URL dan tidak kosong, anggap itu Base64 (dengan atau tanpa prefix)
            // dan dekode menjadi Bitmap. Gunakan R.drawable.cafeeee sebagai fallback jika decode gagal.
            decodeBase64ToBitmap(cafe.image) ?: R.drawable.cafeeee
        }
    }

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageModel,
                    error = painterResource(id = R.drawable.cafeeee),
                    placeholder = painterResource(id = R.drawable.cafeeee)
                ),
                contentDescription = cafe.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            // Modified: Directly place text at the bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 12.dp, vertical = 12.dp) // Adjusted padding
            ) {
                Text(
                    text = cafe.name,
                    color = Color.White,
                    fontSize = 18.sp, // Adjusted font size slightly
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
@Composable
fun CafeCardVertical(cafe: Cafe, onClick: () -> Unit) {
    val imageModel: Any = remember(cafe.image) {
        if (cafe.image.isNullOrEmpty()) {
            R.drawable.cafeeee // Jika string gambar kosong, gunakan placeholder
        } else if (cafe.image.startsWith("http://") || cafe.image.startsWith("https://")) {
            cafe.image // Jika ini adalah URL, biarkan Coil menanganinya langsung sebagai String
        } else {
            // Jika bukan URL dan tidak kosong, anggap itu Base64 (dengan atau tanpa prefix)
            // dan dekode menjadi Bitmap. Gunakan R.drawable.cafeeee sebagai fallback jika decode gagal.
            decodeBase64ToBitmap(cafe.image) ?: R.drawable.cafeeee
        }
    }

    Card(
        modifier = Modifier
            .width(160.dp) // This sets the width of each individual card
            .height(300.dp) // This sets the height of each individual card
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageModel,
                    error = painterResource(id = R.drawable.cafeeee),
                    placeholder = painterResource(id = R.drawable.cafeeee)
                ),
                contentDescription = cafe.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 12.dp, vertical = 12.dp) // Adjusted padding
            ) {
                Text(
                    text = cafe.name,
                    color = Color.White,
                    fontSize = 18.sp, // Adjusted font size slightly
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,


                    )
            }

        }
    }
}
@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier
            .height(90.dp)
    ) {
        val iconModifier = Modifier.size(20.dp)
        val selectedColor = Color(0xFF5D4037) // Dark brown for selected items
        val unselectedColor = Color.Gray // Gray for unselected items

        // Home/Beranda
        NavigationBarItem(
            selected = currentRoute == "welcome", // Check if current route is "welcome"
            onClick = {
                if (currentRoute != "welcome") { // Avoid re-navigating if already on the same screen
                    navController.navigate("welcome") {
                        // Pop up to the start destination of the graph to avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = "Beranda",
                    modifier = iconModifier
                )
            },
            label = { Text("Beranda") }, // Add text label
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                selectedTextColor = selectedColor,
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor,
                indicatorColor = Color.Transparent // As per image, no explicit indicator
            )
        )

        // History/Riwayat
        NavigationBarItem(
            selected = currentRoute == "history", // Check if current route is "history"
            onClick = {
                if (currentRoute != "history") {
                    navController.navigate("history") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.riwayat),
                    contentDescription = "Riwayat",
                    modifier = iconModifier
                )
            },
            label = { Text("Riwayat") }, // Add text label
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                selectedTextColor = selectedColor,
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor,
                indicatorColor = Color.Transparent
            )
        )

        // Profile/Profil
        NavigationBarItem(
            selected = currentRoute == "profile", // Check if current route is "profile"
            onClick = {
                if (currentRoute != "profile") {
                    navController.navigate("profile") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = "Profil",
                    modifier = iconModifier
                )
            },
            label = { Text("Profil") }, // Add text label
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                selectedTextColor = selectedColor,
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor,
                indicatorColor = Color.Transparent
            )
        )
    }
}