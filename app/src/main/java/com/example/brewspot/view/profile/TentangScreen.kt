package com.example.brewspot.view.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.brewspot.R
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TentangScreen(navController: NavController) {
    val brownColor = Color(0xFF5D4037)
    val lightGrayBackground = Color(0xFFF0F0F0)
    val darkGrayText = Color(0xFF424242)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tentang BrewSpot",
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
        },
        containerColor = lightGrayBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logobrewjos),
                    contentDescription = "BrewSpot Logo",
                    modifier = Modifier.size(200.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "Versi 1.0.0",
                    fontSize = 16.sp,
                    color = darkGrayText
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Apa itu BrewSpot?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = brownColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "BrewSpot adalah aplikasi pendamping sempurna bagi para pecinta kopi dan kafe di Malang. Kami hadir untuk memudahkan Anda menemukan kafe terbaik, melakukan reservasi, dan menikmati pengalaman 'ngopi' tanpa hambatan.",
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = darkGrayText,
                        textAlign = TextAlign.Justify
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Fitur Unggulan:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = brownColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // List of features
                    Text(
                        text =  "1. Pencarian Kafe: Temukan kafe berdasarkan lokasi, popularitas, atau rekomendasi khusus.\n" +
                                "2. Detail Kafe Lengkap: Lihat informasi jam operasional, alamat, fasilitas, dan galeri foto.\n" +
                                "3. Reservasi Mudah: Pesan meja di kafe favorit Anda dengan cepat dan praktis.\n" +
                                "4. Riwayat Reservasi: Lacak semua reservasi Anda dengan mudah.\n" +
                                "5. Profil Pengguna: Kelola informasi akun Anda dengan aman.",
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = darkGrayText
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Visi Kami:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = brownColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Menjadi jembatan antara komunitas kopi dan kafe di Malang, menciptakan pengalaman bersosialisasi yang lebih baik dan mendukung pertumbuhan kafe lokal.",
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = darkGrayText,
                        textAlign = TextAlign.Justify
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Terima kasih telah menggunakan BrewSpot!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = brownColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "© 2024 BrewSpot. All rights reserved.",
                fontSize = 12.sp,
                color = darkGrayText.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}