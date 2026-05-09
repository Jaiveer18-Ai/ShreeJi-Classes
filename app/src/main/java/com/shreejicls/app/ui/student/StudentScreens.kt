package com.shreejicls.app.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shreejicls.app.ui.components.*
import com.shreejicls.app.ui.theme.*
import com.shreejicls.app.util.DateUtils

@Composable
fun StudentNotesScreen(vm: StudentViewModel) {
    val notes by vm.allNotes.collectAsState()
    var filterSubject by remember { mutableStateOf("All") }
    val subjects = listOf("All") + notes.map { it.subject }.distinct()
    val filtered = if (filterSubject == "All") notes else notes.filter { it.subject == filterSubject }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            subjects.take(5).forEach { s ->
                FilterChip(selected = filterSubject == s, onClick = { filterSubject = s }, label = { Text(s) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SaffronOrange, selectedLabelColor = androidx.compose.ui.graphics.Color.White))
            }
        }
        if (filtered.isEmpty()) EmptyState(Icons.Outlined.MenuBook, "No Notes", "Notes will appear here")
        else LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
            items(filtered) { note ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row { StatusChip(text = note.type, color = InfoBlue); Spacer(Modifier.width(8.dp)); StatusChip(text = note.subject, color = LeafGreen) }
                        Spacer(Modifier.height(8.dp))
                        Text(note.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                        if (note.topic.isNotBlank()) Text(note.topic, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (note.description.isNotBlank()) { Spacer(Modifier.height(4.dp)); Text(note.description, style = MaterialTheme.typography.bodyMedium) }
                        Spacer(Modifier.height(4.dp)); Text(DateUtils.getRelativeTime(note.uploadDate), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun StudentTestsScreen(vm: StudentViewModel) {
    val tests by vm.allTests.collectAsState()
    if (tests.isEmpty()) EmptyState(Icons.Outlined.EventNote, "No Tests", "Scheduled tests will appear here")
    else LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)) {
        items(tests) { test ->
            val isPast = test.date < System.currentTimeMillis()
            val days = DateUtils.daysUntil(test.date)
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row { StatusChip(text = test.subject, color = SaffronOrange); Spacer(Modifier.width(8.dp)); StatusChip(text = if (isPast) "Done" else if (days <= 1) "Tomorrow!" else "In ${days}d", color = if (isPast) MediumGray else if (days <= 2) ErrorRed else LeafGreen) }
                    Spacer(Modifier.height(8.dp))
                    Text(test.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text("${DateUtils.formatDate(test.date)} at ${test.time}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (test.syllabus.isNotBlank()) { Spacer(Modifier.height(4.dp)); Text("Syllabus: ${test.syllabus}", style = MaterialTheme.typography.bodySmall) }
                    Text("Total Marks: ${test.totalMarks}", style = MaterialTheme.typography.labelMedium, color = SaffronOrange)
                }
            }
        }
    }
}

@Composable
fun StudentFeesScreen(vm: StudentViewModel) {
    val fees by vm.myFees.collectAsState()
    if (fees.isEmpty()) EmptyState(Icons.Outlined.Receipt, "No Fee Records", "Fee records will appear here")
    else LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)) {
        item { Text("Fee History", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) }
        items(fees) { fee ->
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${DateUtils.getMonthName(fee.month)} ${fee.year}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("₹${fee.amount.toInt()}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = SaffronOrange))
                        if (fee.paidDate != null) Text("Paid on ${DateUtils.formatDate(fee.paidDate)}", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                    }
                    StatusChip(text = fee.status, color = if (fee.status == "PAID") SuccessGreen else ErrorRed)
                }
            }
        }
    }
}

@Composable
fun StudentProgressScreen(vm: StudentViewModel) {
    val results by vm.myResults.collectAsState()
    if (results.isEmpty()) EmptyState(Icons.Outlined.TrendingUp, "No Results Yet", "Your marks will appear here")
    else {
        LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)) {
            // Chart
            if (results.size >= 2) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Performance Trend", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(Modifier.height(12.dp))
                            SimpleBarChart(data = results.takeLast(5).reversed().mapIndexed { i, r -> "T${i + 1}" to (r.marksObtained.toFloat() / r.totalMarks * 100) })
                        }
                    }
                }
            }
            item { Text("All Results", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) }
            items(results) { r ->
                val pct = (r.marksObtained.toFloat() / r.totalMarks * 100).toInt()
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${r.marksObtained}/${r.totalMarks}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = SaffronOrange))
                            if (r.remarks.isNotBlank()) Text(r.remarks, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(DateUtils.formatDate(r.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusChip(text = "$pct%", color = when { pct >= 80 -> SuccessGreen; pct >= 50 -> WarningAmber; else -> ErrorRed })
                    }
                }
            }
        }
    }
}
