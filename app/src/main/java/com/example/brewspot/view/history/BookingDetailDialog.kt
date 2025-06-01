package com.example.brewspot.view.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.brewspot.R // Sesuaikan dengan R.drawable.cafeeee atau placeholder Anda
import com.example.brewspot.view.cafe_detail.loadImageModel // Pastikan ini diimpor
import java.text.NumberFormat
import java.util.Locale
import com.example.brewspot.utils.formatRupiah // <-- TAMBAHKAN INI


@Composable
fun BookingDetailDialog(
    booking: BookingHistory,
    onDismiss: () -> Unit
) {
    val brownColor = Color(0xFF5D4037)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cafe Image
                val imageModel = loadImageModel(booking.cafeImageUrl)
                val painter = rememberAsyncImagePainter(
                    model = imageModel,
                    placeholder = painterResource(id = R.drawable.cafeeee),
                    error = painterResource(id = R.drawable.cafeeee)
                )
                Image(
                    painter = painter,
                    contentDescription = booking.cafeName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ringkasan Pemesanan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Detail Pemesanan dari Firestore (disesuaikan dengan field baru)
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DetailRow("Nama Kafe:", booking.reservationCafeName ?: booking.cafeName)
                    DetailRow("ID Booking:", booking.reservationId ?: booking.id)
                    DetailRow("Tanggal Booking:", booking.date) // Tanggal booking awal
                    DetailRow("Waktu Reservasi:", booking.reservationTime ?: booking.time) // Waktu reservasi
                    DetailRow("Jumlah Tamu:", (booking.reservationTotalGuests ?: booking.totalGuests).toString())
                    DetailRow("Meja Dipilih:", (booking.reservationSelectedTables ?: booking.selectedTables)?.joinToString(", ") ?: "N/A")
                    booking.paymentMethod?.let { method ->
                        DetailRow("Metode Pembayaran:", method)
                    }
                    DetailRow("Status:", booking.status, statusColor = when(booking.status) {
                        "Sudah Dibayar" -> Color(0xFF4CAF50)
                        "Dibatalkan" -> Color(0xFFE53935)
                        "Kadaluarsa" -> Color(0xFF757575)
                        else -> Color.Gray
                    })

                    Spacer(modifier = Modifier.height(8.dp))

                    // Detail Harga
                    DetailRow("Uang Muka Pemesanan Kafe:", formatRupiah(booking.downpaymentAmount))
                    DetailRow("Biaya Aplikasi:", formatRupiah(booking.appFeeAmount))

                    // Detail Voucher (jika ada)
                    booking.voucherName?.let { voucherName ->
                        booking.voucherPotongan?.let { voucherPotongan ->
                            DetailRow("Voucher:", "$voucherName (-${formatRupiah(voucherPotongan)})")
                        }
                    }

                    // Detail Items
                    if (booking.items.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Item Pesanan:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Column(modifier = Modifier.fillMaxWidth()) {
                            booking.items.forEach { item ->
                                val itemName = item["name"] as? String ?: "Unknown Item"
                                val quantity = item["quantity"] as? Long ?: 0L
                                val price = item["priceAtOrder"] as? Double ?: 0.0
                                DetailRow(" - $itemName (${quantity}x)", formatRupiah(price * quantity))
                            }
                        }
                    }
                }

                Divider(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    color = Color.Gray,
                    thickness = 1.dp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Pembayaran:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = formatRupiah(booking.totalPrice),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = brownColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = brownColor)
                ) {
                    Text("Oke", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, statusColor: Color? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.DarkGray)
        if (statusColor != null) {
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
        } else {
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}