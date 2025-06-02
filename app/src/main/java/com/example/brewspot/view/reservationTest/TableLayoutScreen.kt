package com.example.brewspot.view.reservationTest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material3.FabPosition
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter

import com.example.brewspot.R
import com.example.brewspot.view.cafe_detail.loadImageModel
import kotlinx.coroutines.flow.distinctUntilChanged

class CustomTopShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = Outline.Generic(Path().apply {
        val width = size.width
        val height = size.height

        moveTo(0f, 0f)
        lineTo(width, 0f)
        lineTo(width, height * 0.75f)
        quadraticBezierTo(
            width / 2, height * 1.1f,
            0f, height * 0.75f
        )
        close()
    })
}


@Composable
fun TableItem(
    tableId: String,
    isBooked: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isBooked -> Color(0xFFE53935)
        isSelected -> Color.DarkGray
        else -> Color.White
    }
    val borderColor = if (isBooked || isSelected) backgroundColor else Color.Gray
    val textColor = when {
        isBooked -> Color.White
        isSelected -> Color.White
        else -> Color.Black
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable(enabled = !isBooked) { onClick() }
            .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = tableId, color = textColor, fontSize = 12.sp)
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color, RoundedCornerShape(4.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = Color.Black)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableLayoutScreen(
    navController: NavController,
    cafeId: String?,
    userName: String?,
    date: String?,
    time: String?,
    totalGuests: Int,
    viewModel: TableViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    val tables by viewModel.tables.collectAsState()
    val selectedTables = viewModel.selectedTables
    val cafe by viewModel.cafe.collectAsState()

    val primaryBrown = Color(0xFF4E342E)
    val lighterGrey = Color(0xFFF0F0F0)

    val scrollState = rememberScrollState()
    val fabVisible = remember { mutableStateOf(true) }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                if (isScrolling) {
                    fabVisible.value = false
                } else {
                    fabVisible.value = true
                }
            }
    }


    LaunchedEffect(cafeId, userName, date, time, totalGuests) {
        viewModel.setReservationDetails(cafeId, userName, date, time, totalGuests)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshTableStatusIfCafeIdSet()
                println("TableLayoutScreen: ON_RESUME, refreshing table status.")
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Pemilihan Meja",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryBrown)
            )
        },
        floatingActionButton = {
            val selectedTableCount = selectedTables.size
            val canProceed = selectedTableCount > 0

            val fabContainerColor = if (canProceed) primaryBrown else primaryBrown.copy(alpha = 0.5f)
            val fabContentColor = if (canProceed) Color.White else Color.White.copy(alpha = 0.7f)

            val onClickAction: (() -> Unit)? = if (canProceed) {
                {
                    if (cafeId != null && userName != null && date != null && time != null) {
                        viewModel.createReservation(
                            cafeId = cafeId,
                            cafeName = cafe?.name ?: "Unknown Cafe",
                            userName = userName,
                            date = date,
                            totalGuests = totalGuests,
                            time = time,
                            onSuccess = { reservationId ->
                                Toast.makeText(context, "Reservasi berhasil! Silakan pilih menu.", Toast.LENGTH_SHORT).show()
                                navController.navigate("menu/${cafeId}?reservationId=${reservationId}") {
                                    popUpTo("welcome") {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            },
                            onFailure = { errorMessage ->
                                Toast.makeText(context, "Reservasi gagal: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Data reservasi tidak lengkap.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                null
            }

            AnimatedVisibility(
                visible = fabVisible.value,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                ExtendedFloatingActionButton(
                    onClick = onClickAction ?: {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    containerColor = fabContainerColor,
                    shape = RoundedCornerShape(8.dp),
                    contentColor = fabContentColor
                ) {
                    Text("Selanjutnya (${selectedTableCount} Meja Dipilih)", color = Color.White, fontSize = 18.sp)
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(lighterGrey)
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(horizontal = 16.dp)
                    .background(Color(0xFFF5F5DC), RoundedCornerShape(16.dp))
                    .clipToBounds()
            ) {
                val denahImageModel = remember(cafe?.denahImage) { loadImageModel(cafe?.denahImage) }
                val denahPainter = rememberAsyncImagePainter(
                    model = denahImageModel,
                    placeholder = painterResource(id = R.drawable.cafeeee),
                    error = painterResource(id = R.drawable.cafeeee)
                )

                Image(
                    painter = denahPainter,
                    contentDescription = "Denah Cafe",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "Keterangan:",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                LegendItem(color = Color.White, text = "Meja kosong")
                Spacer(modifier = Modifier.height(4.dp))
                LegendItem(color = Color.DarkGray, text = "Meja yang dipilih")
                Spacer(modifier = Modifier.height(4.dp))
                LegendItem(color = Color(0xFFE53935), text = "Meja terisi")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Pilih Meja Anda:",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filteredAndSortedTables = tables.entries
                    .filter { (id, _) -> id != "PHOTO BOOTH" && id != "KASIR" && id != "TEMPAT PARKIR" }
                    .sortedBy { it.key.replace("T", "").toIntOrNull() ?: Int.MAX_VALUE }

                items(filteredAndSortedTables) { (id, isBooked) ->
                    TableItem(
                        tableId = id,
                        isBooked = isBooked,
                        isSelected = viewModel.selectedTables.contains(id),
                        onClick = {
                            if (!isBooked) {
                                viewModel.toggleTableSelection(id)
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}