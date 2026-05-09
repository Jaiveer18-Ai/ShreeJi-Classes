@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.shreejicls.app.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shreejicls.app.ui.components.*
import com.shreejicls.app.ui.theme.*
import com.shreejicls.app.util.DateUtils

@Composable
fun FeesManagementScreen(vm: TeacherViewModel) {
    val allFees by vm.allFees.collectAsState()
    val students by vm.students.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var filterStatus by remember { mutableStateOf("ALL") }
    val filtered = when (filterStatus) {
        "PAID" -> allFees.filter { it.status == "PAID" }
        "UNPAID" -> allFees.filter { it.status != "PAID" }
        else -> allFees
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALL" to "All", "UNPAID" to "Unpaid", "PAID" to "Paid").forEach { (k, l) ->
                FilterChip(selected = filterStatus == k, onClick = { filterStatus = k }, label = { Text(l) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SaffronOrange, selectedLabelColor = Color.White))
            }
        }
        if (filtered.isEmpty()) { EmptyState(Icons.Outlined.ReceiptLong, "No Fee Records", "Add fee records for students") }
        else {
            LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                items(filtered) { fee ->
                    val sName = students.find { it.userId == fee.studentId }?.name ?: fee.studentId
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text("${DateUtils.getMonthName(fee.month)} ${fee.year}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${fee.amount.toInt()}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = SaffronOrange))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                StatusChip(text = fee.status, color = if (fee.status == "PAID") SuccessGreen else ErrorRed)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (fee.status != "PAID") {
                                    Button(onClick = { vm.markFeePaid(fee.feeId, fee.amount) }, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        contentPadding = PaddingValues(horizontal = 12.dp), modifier = Modifier.height(32.dp)) { Text("Mark Paid", style = MaterialTheme.typography.labelSmall) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(onClick = { showAddDialog = true }, modifier = Modifier.padding(24.dp), containerColor = SaffronOrange, contentColor = Color.White) { Icon(Icons.Filled.Add, null) }
    }
    if (showAddDialog) {
        var selStu by remember { mutableStateOf("") }; var month by remember { mutableStateOf("") }; var year by remember { mutableStateOf("2026") }; var amount by remember { mutableStateOf("1500") }; var exp by remember { mutableStateOf(false) }
        AlertDialog(onDismissRequest = { showAddDialog = false }, title = { Text("Add Fee Record", fontWeight = FontWeight.Bold) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = exp, onExpandedChange = { exp = it }) {
                    OutlinedTextField(value = students.find { it.userId == selStu }?.name ?: "", onValueChange = {}, readOnly = true, label = { Text("Student") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(exp) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp))
                    ExposedDropdownMenu(expanded = exp, onDismissRequest = { exp = false }) { students.forEach { s -> DropdownMenuItem(text = { Text("${s.name} (${s.userId})") }, onClick = { selStu = s.userId; exp = false }) } }
                }
                OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("Month (1-12)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (₹)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            } },
            confirmButton = { Button(onClick = { val m = month.toIntOrNull(); val y = year.toIntOrNull(); val a = amount.toDoubleOrNull(); if (m != null && y != null && a != null && selStu.isNotBlank() && m in 1..12) { vm.addFee(selStu, m, y, a); showAddDialog = false } }, colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)) { Text("Add") } },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } })
    }
}
