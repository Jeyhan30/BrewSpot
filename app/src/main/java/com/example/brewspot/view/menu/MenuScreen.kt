// app/src/main/java/com/example/brewspot/view/menu/MenuScreen.kt
package com.example.brewspot.view.menu

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.brewspot.R // Pastikan R diimpor dengan benar
import com.example.brewspot.view.home.CustomTopShape // Re-using CustomTopShape from HomeScreen (if exists)
import java.text.NumberFormat
import java.util.Locale

// Re-using the decodeBase64ToBitmap function from HomeScreen or ProfileScreen
fun decodeBase64ToBitmap(base64Str: String?): Bitmap? {
    if (base64Str.isNullOrEmpty()) {
        return null
    }
    val cleanBase64Str = if (base64Str.startsWith("data:image", ignoreCase = true)) {
        base64Str.substringAfter(",")
    } else {
        base64Str
    }
    return try {
        val decodedBytes = Base64.decode(cleanBase64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    navController: NavController,
    cafeId: String, // Receive cafeId from navigation
    menuViewModel: MenuViewModel// Injeksi ViewModel
) {
    val cafeDetails by menuViewModel.cafeDetails.collectAsState()
    val menuItems by menuViewModel.menuItems.collectAsState()
    val cartItems by menuViewModel.cartItems.collectAsState() // Observe cart items
    val totalPrice by menuViewModel.totalPrice.collectAsState() // Observe total price

    var searchQuery by remember { mutableStateOf("") } // State for search bar

    val brownColor = Color(0xFF5D4037)

    // Muat data cafe dan menu saat komponen pertama kali dibuat atau cafeId berubah
    LaunchedEffect(cafeId) {
        menuViewModel.fetchCafeAndMenuItems(cafeId)
    }

    Scaffold(
        topBar = {
            // No TopAppBar needed if the image is the top bar.
            // If you want a traditional AppBar, uncomment below and adjust.
        },
        bottomBar = {
            // "Selanjutnya" button acts as a bottom bar
            BottomAppBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.height(80.dp)
            ) {
                Button(
                    onClick = {
                        // TODO: Implement navigation to next screen (e.g., checkout/cart summary)
                        // Anda bisa meneruskan cartItems ke layar berikutnya
                        // Atau langsung memanggil menuViewModel.checkout() jika tombol ini langsung untuk finalisasi pesanan
                        if (cartItems.isNotEmpty()) {
                            // Contoh navigasi ke layar ringkasan keranjang
                            // Pastikan Anda memiliki rute 'cart_summary_screen' dan dapat menerima objek data
                            // navContoller.navigate("cart_summary_screen") // Ini butuh implementasi penerusan data
                            // Untuk saat ini, kita akan memanggil checkout langsung
                            menuViewModel.checkout()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 24.dp), // Add horizontal padding for the button
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brownColor),
                    enabled = cartItems.isNotEmpty() // Aktifkan tombol hanya jika keranjang tidak kosong
                ) {
                    Text(
                        "Selanjutnya (${formatRupiah(totalPrice)})", // Tampilkan total harga di tombol
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F0F0)) // Light gray background
        ) {
            // Top Section - Cafe Image and Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Height of the image section
            ) {
                val cafeImageModel: Any? = remember(cafeDetails?.image) {
                    val imageString = cafeDetails?.image
                    if (imageString.isNullOrEmpty()) {
                        null
                    } else if (imageString.startsWith("http://") || imageString.startsWith("https://")) {
                        imageString // Ini adalah URL
                    } else if (imageString.startsWith("data:image/")) {
                        decodeBase64ToBitmap(imageString)
                    } else {
                        decodeBase64ToBitmap(imageString) // Coba dekode sebagai Base64 murni
                    }
                }

                val painter = rememberAsyncImagePainter(
                    model = cafeImageModel,
                    placeholder = painterResource(id = R.drawable.cafeeee), // Default placeholder
                    error = painterResource(id = R.drawable.cafeeee) // Default error image
                )

                Image(
                    painter = painter,
                    contentDescription = "Cafe Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay for Back Button, Cafe Name, and Bag Icon
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)) // Semi-transparent overlay
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = cafeDetails?.name ?: "Jokopi", // Display cafe name from ViewModel
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        Box(contentAlignment = Alignment.Center) {
                            IconButton(onClick = {
                                navController.navigate("cart_screen")
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.bag), // Assuming you have this icon
                                    contentDescription = "Shopping Bag",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            if (cartItems.isNotEmpty()) {
                                Text(
                                    text = cartItems.size.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Red)
                                        .padding(horizontal = 8.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
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

            // Menu Section
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp) // Tambahkan padding horizontal ke Column ini
            ) {
                Text(
                    "Menu",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp) // Padding at the bottom of the list
                ) {
                    val filteredMenuItems = menuItems.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                                it.description.contains(searchQuery, ignoreCase = true)
                    }
                    items(filteredMenuItems) { item ->
                        MenuItemCard(item = item) {
                            menuViewModel.addToCart(item) // Hubungkan ke ViewModel
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItemCard(item: MenuItem, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Menu Item Image
                val imagePainter = rememberAsyncImagePainter(
                    model = item.imageUrl,
                    placeholder = painterResource(id = R.drawable.coffee), // Default placeholder
                    error = painterResource(id = R.drawable.coffee) // Default error image
                )
                Image(
                    painter = imagePainter,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(130.dp) // Slightly adjusted size
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Menu Item Details
                Column(
                    modifier = Modifier.weight(1f) // Take remaining space
                ) {

                    Text(
                        text = item.name,
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatRupiah(item.price), // Format harga
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    // "Tambah" Button
                    Button(
                        onClick = onAddClick, // Terhubung ke lambda onAddClick
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)), // Brown color for button
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                        modifier = Modifier
                            .padding(start = 80.dp) // Add some top padding
                            .width(80.dp) // Fixed width for the button
                    ) {
                        Text("Tambah", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// Fungsi helper untuk memformat harga ke dalam Rupiah
fun formatRupiah(number: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(number)
}