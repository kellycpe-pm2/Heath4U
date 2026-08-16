package com.example.healt4u.screen.Medicine
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.healt4u.R

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.model.Medicine
import com.example.healt4u.screen.componentUI.Theme.colorTheme


@Composable
fun MedicineListScreen() {
    val medicines = remember {
        mutableStateListOf(
            Medicine(
                id = 1,
                name_medicine = "Paracetamol 500mg",
                category = "A",
                dosage = 500,
                quantity = 100,
                quantityLeft = 45,
                remark = "Take with food",
                expiredDate = parseDateToLong("15-12-2026"),
                afterEat = true,
                priority = 1
            ),
            Medicine(
                id = 2,
                name_medicine = "Vitamin C 1000mg",
                category = "N",
                dosage = 1000,
                quantity = 50,
                quantityLeft = 12,
                remark = null,
                expiredDate = parseDateToLong("01-01-2027"),
                afterEat = false,
                priority = 0
            ),
            Medicine(
                id = 3,
                name_medicine = "Amoxicillin 250mg",
                category = "X",
                dosage = 250,
                quantity = 30,
                quantityLeft = 3,
                remark = "Expiring soon!",
                expiredDate = parseDateToLong("01-09-2026"),
                afterEat = true,
                priority = 2
            ),
            Medicine(
                id = 4,
                name_medicine = "Herbal Tea Extract",
                category = "T",
                dosage = 200,
                quantity = 20,
                quantityLeft = 20,
                remark = null,
                expiredDate = parseDateToLong("20-12-2026"),
                afterEat = false,
                priority = 0
            ),
            Medicine(
                id = 5,
                name_medicine = "Dog Pain Relief",
                category = "H",
                dosage = 100,
                quantity = 15,
                quantityLeft = 3,
                remark = "Veterinary use only",
                expiredDate = parseDateToLong("10-10-2026"),
                afterEat = true,
                priority = 0
            )
        )
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredMedicines = if (searchQuery.isEmpty()) {
        medicines
    } else {
        medicines.filter { medicine ->
            medicine.name_medicine.contains(searchQuery, ignoreCase = true) ||
                    medicine.category.contains(searchQuery, ignoreCase = true) ||
                    medicine.remark?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    colorTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar

            // List Header with count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Medicines",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                ) {
                    Text(
                        text = "${filteredMedicines.size}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Medicine List
            if (filteredMedicines.isEmpty()) {
                EmptyStateView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMedicines) { medicine ->
                        MedicineRow(medicine, {})
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = "Search",
                modifier = Modifier.size(20.dp)
            )
            TextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        "Search medicines...",
                        color = Color(0xFFB0BEC5),
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.secondary,
                    focusedTextColor = Color(0xFF1A1A2E),
                    unfocusedTextColor = Color(0xFF1A1A2E)
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = Color(0xFF1A1A2E)
                )
            )
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchChange("") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.close),
                        contentDescription = "Clear",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF78909C)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Medicines Found",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the + button to add your first medicine",
            fontSize = 14.sp,
            color = Color(0xFF78909C)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMedicineListScreen() {
        MedicineListScreen()

}