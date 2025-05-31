package com.example.brewspot.view.voucher

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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.brewspot.R
import java.text.NumberFormat
import java.util.Locale

// Fungsi formatRupiah (sudah ada di tempat lain, pastikan bisa diakses atau duplikasi)
fun formatRupiah(number: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(number)
}

// NEW: Fungsi untuk format angka menjadi "Xrb"
fun formatRibuan(amount: Double): String {
    return if (amount >= 1000) {
        val thousands = amount / 1000
        "${thousands}rb"
    } else {
        amount.toString()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherScreen(
    navController: NavController,
    voucherViewModel: VoucherViewModel = viewModel() // Inject ViewModel
) {
    val vouchers by voucherViewModel.vouchers.collectAsState()
    val selectedVoucher by voucherViewModel.selectedVoucher.collectAsState()

    val brownColor = Color(0xFF5D4037)
    val lightGreyBackground = Color(0xFFF0F0F0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Voucher Saya",
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(lightGreyBackground)
                .padding(horizontal = 16.dp, vertical = 8.dp) // Adjusted padding
        ) {
            Spacer(modifier = Modifier.height(30.dp))


            if (vouchers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada voucher yang tersedia.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp), // Spasi antar voucher card
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(vouchers) { voucher ->
                        VoucherCard(
                            voucher = voucher,
                            isSelected = voucher == selectedVoucher,
                            onSelect = { voucherViewModel.selectVoucher(voucher) },
                            onUse = {
                                voucherViewModel.selectVoucher(voucher) // Pilih voucher saat "Pakai" diklik
                                navController.popBackStack() // Kembali ke layar sebelumnya
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoucherCard(
    voucher: Voucher,
    isSelected: Boolean,
    onSelect: (Voucher) -> Unit, // Callback ketika voucher dipilih
    onUse: (Voucher) -> Unit // Callback ketika tombol "Pakai" diklik
) {
    val borderColor = if (isSelected) Color.DarkGray else Color.LightGray
    val backgroundColor = if (isSelected) Color(0xFFFFFBE0) else Color.White // Slightly yellowish if selected

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onSelect(voucher) }, // Klik pada card untuk memilih
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.voucher), // Ganti dengan ikon voucher Anda
                        contentDescription = "Voucher Icon",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = voucher.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // MODIFIED: Menggunakan formatRibuan untuk minimal
                        Text(
                            text = "Min. Pembayaran ${formatRibuan(voucher.minimal)}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        // TODO: Tambahkan tanggal berlaku jika ada di database
                        Text(
                            text = "Berlaku s.d. 21/06/2025", // Contoh tanggal
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                val brownColor = Color(0xFF5D4037)

                Button(
                    onClick = { onUse(voucher) },
                    colors = ButtonDefaults.buttonColors(containerColor = brownColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Pakai", color = Color.White, fontSize = 14.sp)
                }
            }
            val brownColor = Color(0xFF5D4037)

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.LightGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))
            // Tampilkan potongan nominal
            Text(
                text = "Hemat s.d. ${formatRibuan(voucher.potongan)}", // Menampilkan potongan nominal
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = brownColor // Warna untuk teks "Hemat"
            )
        }
    }
}
