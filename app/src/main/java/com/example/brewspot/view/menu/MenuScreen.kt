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
import com.example.brewspot.R
import com.example.brewspot.view.home.CustomTopShape
import java.text.NumberFormat
import java.util.Locale

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
    cafeId: String,
    menuViewModel: MenuViewModel
) {
    val cafeDetails by menuViewModel.cafeDetails.collectAsState()
    val menuItems by menuViewModel.menuItems.collectAsState()
    val cartItems by menuViewModel.cartItems.collectAsState()
    val totalPrice by menuViewModel.totalPrice.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val brownColor = Color(0xFF5D4037)

    LaunchedEffect(cafeId) {
        menuViewModel.fetchCafeAndMenuItems(cafeId)
    }

    Scaffold(
        topBar = {
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.height(80.dp)
            ) {
                Button(
                    onClick = {
                        // MODIFIED: Navigate to cart_screen with cafeId
                        navController.navigate("cart_screen?cafeId=${cafeId}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brownColor),
                    // Enable button only if there are items from the current cafe in the cart
                    enabled = cartItems.any { it.cafeId == cafeId }
                ) {
                    // Display total price for items from the current cafe
                    val currentCafeTotalPrice = cartItems.filter { it.cafeId == cafeId }.sumOf { it.price * it.quantity }
                    Text(
                        "Selanjutnya (${formatRupiah(currentCafeTotalPrice)})",
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
                .background(Color(0xFFF0F0F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val cafeImageModel: Any? = remember(cafeDetails?.image) {
                    val imageString = cafeDetails?.image
                    if (imageString.isNullOrEmpty()) {
                        null
                    } else if (imageString.startsWith("http://") || imageString.startsWith("https://")) {
                        imageString
                    } else if (imageString.startsWith("data:image/")) {
                        decodeBase64ToBitmap(imageString)
                    } else {
                        decodeBase64ToBitmap(imageString)
                    }
                }

                val painter = rememberAsyncImagePainter(
                    model = cafeImageModel,
                    placeholder = painterResource(id = R.drawable.cafeeee),
                    error = painterResource(id = R.drawable.cafeeee)
                )

                Image(
                    painter = painter,
                    contentDescription = "Cafe Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
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
                            text = cafeDetails?.name ?: "Jokopi",
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
                                // MODIFIED: Pass cafeId when navigating to cart_screen
                                navController.navigate("cart_screen?cafeId=${cafeId}")
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.bag),
                                    contentDescription = "Shopping Bag",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            // Only show count of items from the current cafe
                            val itemsInCurrentCafeCart = cartItems.count { it.cafeId == cafeId }
                            if (itemsInCurrentCafeCart > 0) {
                                Text(
                                    text = itemsInCurrentCafeCart.toString(),
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
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
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    val filteredMenuItems = menuItems.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                                it.description.contains(searchQuery, ignoreCase = true)
                    }
                    items(filteredMenuItems) { item ->
                        MenuItemCard(item = item) {
                            menuViewModel.addToCart(item)
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
                val imagePainter = rememberAsyncImagePainter(
                    model = item.imageUrl,
                    placeholder = painterResource(id = R.drawable.coffee),
                    error = painterResource(id = R.drawable.coffee)
                )
                Image(
                    painter = imagePainter,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
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
                        text = formatRupiah(item.price),
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Button(
                        onClick = onAddClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                        modifier = Modifier
                            .padding(start = 80.dp)
                            .width(80.dp)
                    ) {
                        Text("Tambah", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

fun formatRupiah(number: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(number)
}