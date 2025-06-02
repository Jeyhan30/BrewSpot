package com.example.brewspot.view.payment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.brewspot.R
import com.example.brewspot.view.cafe_detail.decodeBase64ToBitmap

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
fun PaymentMethodScreen(
    navController: NavController,
    paymentMethodViewModel: PaymentMethodViewModel = viewModel()
) {
    val paymentMethods by paymentMethodViewModel.paymentMethods.collectAsState()
    val selectedPaymentMethod by paymentMethodViewModel.selectedPaymentMethod.collectAsState()

    val brownColor = Color(0xFF5D4037)
    val lightGreyBackground = Color(0xFFF0F0F0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Metode Pembayaran",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
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
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brownColor),
                    enabled = selectedPaymentMethod != null
                ) {
                    Text("Pilih", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(lightGreyBackground)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(paymentMethods) { method ->
                PaymentMethodCard(
                    method = method,
                    isSelected = method == selectedPaymentMethod,
                    onSelect = { paymentMethodViewModel.selectPaymentMethod(method) }
                )
            }
        }
    }
}

@Composable
fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onSelect: (PaymentMethod) -> Unit
) {
    val borderColor = if (isSelected) Color.DarkGray else Color.LightGray
    val backgroundColor = if (isSelected) Color(0xFFFFFBE0) else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onSelect(method) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val imageModel: Any? = remember(method.imageUrl) {
                    val imageString = method.imageUrl
                    if (imageString.isNullOrEmpty()) {
                        null
                    } else if (imageString.startsWith("http://") || imageString.startsWith("https://")) {
                        imageString
                    } else {
                        decodeBase64ToBitmap(imageString)
                    }
                }

                val painter = rememberAsyncImagePainter(
                    model = imageModel,
                    placeholder = painterResource(id = R.drawable.coffee),
                    error = painterResource(id = R.drawable.coffee)
                )

                Image(
                    painter = painter,
                    contentDescription = method.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = method.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
            if (isSelected) {
                Icon(
                    painter = painterResource(id = R.drawable.check),
                    contentDescription = "Selected",
                    tint = Color.Green,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPaymentMethodScreen() {
    val navController = rememberNavController()
    PaymentMethodScreen(navController = navController)
}