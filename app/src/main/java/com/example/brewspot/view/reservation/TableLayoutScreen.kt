package com.example.brewspot.view.reservatio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.brewspot.view.reservation.TableViewModel

@Composable
fun TableItem(tableId: String, isBooked: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isBooked) Color.DarkGray else Color.White
    val textColor = if (isBooked) Color.White else Color.Black

    Box(
        modifier = Modifier
            .size(60.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable(enabled = !isBooked) { onClick() }
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = tableId, color = textColor)
    }
}
@Composable
fun TableLayoutScreen(viewModel: TableViewModel, navController: NavController) {
    val tables by viewModel.tables.collectAsState()

    // State untuk track meja yang dipilih saat ini
    var selectedTable by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            "CHECKOUT & CHOOSE YOUR LAYOUT",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFFFFA726)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text("JOKOPI", style = MaterialTheme.typography.bodyLarge, color = Color.White)
        Text("JL. JAKARTA NO.26", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tables.toList()) { (id, isBooked) ->
                TableItem(
                    tableId = id,
                    isBooked = isBooked,
                    isSelected = selectedTable == id,
                    onClick = {
                        if (!isBooked) {
                            selectedTable = if (selectedTable == id) null else id
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (selectedTable != null) {
                    viewModel.selectedTable = selectedTable
                    viewModel.bookSelectedTable()
                    selectedTable = null
                    navController.popBackStack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
        ) {
            Text("CONFIRM", color = Color.Black)
        }
    }
}

@Composable
fun TableItem(tableId: String, isBooked: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = when {
        isBooked -> Color.DarkGray
        isSelected -> Color.Black
        else -> Color.White
    }
    val textColor = when {
        isBooked -> Color.White
        isSelected -> Color.White
        else -> Color.Black
    }

    Box(
        modifier = Modifier
            .size(60.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable(enabled = !isBooked) { onClick() }
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = tableId, color = textColor)
    }
}
