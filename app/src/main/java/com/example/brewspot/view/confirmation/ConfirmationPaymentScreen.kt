package com.example.brewspot.view.confirmation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.brewspot.R
import com.example.brewspot.view.home.Cafe
import com.example.brewspot.view.menu.MenuItem
import com.example.brewspot.view.cafe_detail.loadImageModel
import com.example.brewspot.view.menu.MenuViewModel
import java.text.NumberFormat
import java.util.Locale
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog
import com.example.brewspot.view.home.decodeBase64ToBitmap
import com.example.brewspot.view.payment.PaymentMethodViewModel

fun formatRupiah(number: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(number)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationPaymentScreen(
    navController: NavController,
    cafeId: String,
    reservationId: String,
    viewModel: ConfirmationViewModel,
    menuViewModel: MenuViewModel,
    paymentMethodViewModel: PaymentMethodViewModel

) {
    val cafeDetails by viewModel.cafeDetails.collectAsState()
    val reservationDetails by viewModel.reservationDetails.collectAsState()
    val orderedMenuItems by viewModel.orderedMenuItems.collectAsState()
    val totalPayment by viewModel.totalPayment.collectAsState()
    val currentMenuCartItems by menuViewModel.cartItems.collectAsState()
    val calculatedDownPayment by viewModel.calculatedDownPayment.collectAsState()
    val appliedVoucher by viewModel.appliedVoucher.collectAsState()
    val selectedPaymentMethod by paymentMethodViewModel.selectedPaymentMethod.collectAsState()
    val showSuccessDialog = remember { mutableStateOf(false) }
    val brownColor = Color(0xFF5D4037)
    val lightGreyBackground = Color(0xFFF0F0F0)
    val context = LocalContext.current
    val isPaymentMethodSelected = selectedPaymentMethod != null

    LaunchedEffect(cafeId, reservationId, currentMenuCartItems) {
        Log.d("ConfPaymentScreen", "LaunchedEffect triggered. Calling fetchConfirmationDetails.")
        viewModel.fetchConfirmationDetails(cafeId, reservationId, menuViewModel)
    }
    LaunchedEffect(viewModel) {
        viewModel.onFinalCheckoutSuccess = {
            menuViewModel.clearCartForCafe(cafeId)
            showSuccessDialog.value = true
        }
        viewModel.onFinalCheckoutFailure = { errorMessage ->
            Toast.makeText(context, "Pembayaran gagal: $errorMessage", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Konfirmasi Pembayaran",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = brownColor)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.height(80.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Pembayaran", color = Color.Gray, fontSize = 12.sp)
                        Text(formatRupiah(totalPayment), color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (isPaymentMethodSelected) {
                                viewModel.performCheckout(
                                    cafeId = cafeId,
                                    reservationId = reservationId,
                                    orderedItems = orderedMenuItems,
                                    selectedPaymentMethod = selectedPaymentMethod
                                )
                            } else {
                                Toast.makeText(context, "Pilih metode pembayaran terlebih dahulu", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = isPaymentMethodSelected,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = brownColor,
                            disabledContainerColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Bayar Sekarang", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(lightGreyBackground)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {

            item {
                Spacer(modifier = Modifier.height(30.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                "Detail Pemesanan Kafe",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.LightGray, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val imageModel = remember(cafeDetails?.image) { loadImageModel(cafeDetails?.image) }
                            val painter = rememberAsyncImagePainter(
                                model = imageModel,
                                placeholder = painterResource(id = R.drawable.cafeeee),
                                error = painterResource(id = R.drawable.cafeeee)
                            )
                            Image(
                                painter = painter,
                                contentDescription = "Cafe Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .height(90.dp)
                                    .width(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    cafeDetails?.name ?: "Nama Kafe",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                reservationDetails?.let { details ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(painterResource(id = R.drawable.user), contentDescription = "User Icon", modifier = Modifier.size(16.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            details["userName"] as? String ?: "Nama Pemesan",
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(painterResource(id = R.drawable.calendar), contentDescription = "Date Icon", modifier = Modifier.size(16.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            details["date"] as? String ?: "Tanggal",
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(painterResource(id = R.drawable.people), contentDescription = "Guests Icon", modifier = Modifier.size(16.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "${details["totalGuests"] as? Long ?: 0} orang",
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(13.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                reservationDetails?.let { details ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(painterResource(id = R.drawable.clock), contentDescription = "Time Icon", modifier = Modifier.size(16.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            details["time"] as? String ?: "Waktu",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(painterResource(id = R.drawable.table), contentDescription = "Table Icon", modifier = Modifier.size(16.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val selectedTables = details["selectedTables"] as? List<String> ?: emptyList()
                                        Text(
                                            "Meja: ${selectedTables.joinToString(", ")}",
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Menu yang Dipesan
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Menu yang Dipesan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.LightGray, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (orderedMenuItems.isNotEmpty()) {
                            orderedMenuItems.forEach { item ->
                                MenuItemConfirmationCard(item)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        } else {
                            Text("Tidak ada menu yang dipesan.", color = Color.Gray, modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
                        }
                    }
                }
            }
            // Metode Pembayaran
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("paymentMethodScreen") }, // Navigasi ke PaymentMethodScreen
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Metode Pembayaran",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            selectedPaymentMethod?.let { method ->
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
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = method.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Gray
                                    )
                                }
                            } ?: run {
                                Text(
                                    text = "Pilih Metode Pembayaran Anda",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Gray
                                )
                            }
                        }
                        Icon(
                            painterResource(id = R.drawable.rightarrow),
                            contentDescription = "Pilih Metode Pembayaran",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Voucher Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("voucherScreen") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        appliedVoucher?.let { voucher ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = voucher.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "- ${formatRupiah(voucher.potongan)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                            }
                            // Tombol "Ganti Voucher"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            bottomStart = 12.dp,
                                            bottomEnd = 12.dp
                                        )
                                    )
                                    .background(brownColor)
                                    .clickable { navController.navigate("voucherScreen") },
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Ganti Voucher",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        painterResource(id = R.drawable.rightarrow),
                                        contentDescription = "Ganti Voucher",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        } ?: run {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Pilih Voucher Anda",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Icon(
                                    painterResource(id = R.drawable.rightarrow),
                                    contentDescription = "Pilih Voucher Anda",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
            // Ringkasan Pembayaran
            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Ringkasan Pembayaran",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.LightGray, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        val totalMenuOrderPrice = orderedMenuItems.sumOf { it.price * it.quantity }
                        PaymentSummaryRow("Total Pemesanan Menu", totalMenuOrderPrice)
                        PaymentSummaryRow("Biaya Aplikasi", viewModel.appFeeAmount)
                        viewModel.appliedVoucher.collectAsState().value?.let { voucher ->
                            if (totalMenuOrderPrice + viewModel.appFeeAmount >= voucher.minimal) {
                                PaymentSummaryRow("Diskon Voucher", -voucher.potongan, isDiscount = true)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        PaymentSummaryRow("Sistem Down Payment (50%)", calculatedDownPayment)

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.LightGray, thickness = 1.dp)
                        Spacer(modifier =Modifier.height(8.dp))

                        PaymentSummaryRow("Total Pembayaran", totalPayment, isTotal = true)
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))

            }


        }
    }
    if (showSuccessDialog.value) {
        PaymentSuccessDialog(navController = navController) {
            showSuccessDialog.value = false
            navController.navigate("welcome") {
                popUpTo("login") { inclusive = false }
            }
        }
    }

}
@Composable
fun MenuItemConfirmationCard(item: MenuItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f), // Beri bobot agar mengisi ruang
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imagePainter = rememberAsyncImagePainter(
                model = item.imageUrl,
                placeholder = painterResource(id = R.drawable.coffee), // Placeholder
                error = painterResource(id = R.drawable.coffee) // Error placeholder
            )
            Image(
                painter = imagePainter,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f, fill = false)
                    .widthIn(max = 180.dp)
            ) {
                Text(
                    text = item.name,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.description,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatRupiah(item.price * item.quantity),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }

        // Bagian Kuantitas (paling kanan)
        Text(
            text = "${item.quantity}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
        )
    }
}
@Composable
fun PaymentSummaryRow(label: String, amount: Double, isTotal: Boolean = false, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isTotal) 18.sp else 14.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isTotal) Color.Black else if (isDiscount) Color(0xFF388E3C) else Color.Gray // Green for discount
        )
        Text(
            text = if (isDiscount) "- ${formatRupiah(kotlin.math.abs(amount))}" else formatRupiah(amount),
            fontSize = if (isTotal) 18.sp else 14.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isTotal) Color.Black else if (isDiscount) Color(0xFF388E3C) else Color.Black // Green for discount
        )
    }
}
@Composable
fun PaymentSuccessDialog(navController: NavController, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.correct),
                    contentDescription = "Success",
                    modifier = Modifier.size(96.dp),
                    colorFilter = null
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Pembayaran Berhasil",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Anda dapat melanjutkan ke aktivitas lainnya",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
                ) {
                    Text("Lanjutkan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}