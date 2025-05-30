package com.example.brewspot.view.menu



import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    menuViewModel: MenuViewModel = viewModel() // Menggunakan ViewModel yang sama
) {
    val cartItems by menuViewModel.cartItems.collectAsState()
    val totalPrice by menuViewModel.totalPrice.collectAsState()
    val brownColor = Color(0xFF5D4037)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keranjang Belanja Anda") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali ke Menu",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = brownColor)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.height(80.dp)
            ) {
                Button(
                    onClick = { menuViewModel.checkout() }, // Panggil checkout dari ViewModel
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brownColor),
                    enabled = cartItems.isNotEmpty() // Aktifkan tombol hanya jika keranjang tidak kosong
                ) {
                    Text(
                        "Bayar Sekarang (${formatRupiah(totalPrice)})",
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
                .padding(16.dp)
        ) {
            if (cartItems.isEmpty()) {
                Text(
                    "Keranjang Anda kosong. Mari tambahkan beberapa menu!",
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(vertical = 32.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemDetailCard(
                            item = item,
                            onAddQuantity = { menuViewModel.addToCart(item) },
                            onRemoveQuantity = { menuViewModel.removeFromCart(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemDetailCard(item: MenuItem, onAddQuantity: () -> Unit, onRemoveQuantity: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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
                    .size(90.dp)
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
                    maxLines = 1
                )

                Text(
                    text = formatRupiah(item.price),
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Subtotal: ${formatRupiah(item.price * item.quantity)}",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onRemoveQuantity,
                    enabled = item.quantity > 0 // Tombol minus dinonaktifkan jika quantity 0
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Kurangi")
                }
                Text(
                    text = "${item.quantity}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                IconButton(onClick = onAddQuantity) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah")
                }
            }
        }
    }
}
