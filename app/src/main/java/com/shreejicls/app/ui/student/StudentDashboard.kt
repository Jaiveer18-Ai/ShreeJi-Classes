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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shreejicls.app.ui.components.*
import com.shreejicls.app.ui.theme.*
import com.shreejicls.app.util.DateUtils

@Composable
fun StudentDashboard(vm: StudentViewModel, studentName: String, onNavigate: (Int) -> Unit) {
    val fees by vm.myFees.collectAsState()
    val tests by vm.upcomingTests.collectAsState()
    val results by vm.myResults.collectAsState()
    val notes by vm.allNotes.collectAsState()
    val notifications by vm.myNotifications.collectAsState()

    val pendingFees = fees.count { it.status != "PAID" }
    val latestResult = results.firstOrNull()

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Welcome back! 👋", style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                Text(studentName, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
        // Fee + Test cards
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardCard(title = "Fee Status", value = if (pendingFees > 0) "₹Due" else "✓ Paid",
                    icon = Icons.Filled.CurrencyRupee, gradientColors = if (pendingFees > 0) listOf(ErrorRed.copy(0.9f), ErrorRed) else listOf(GradientGreenStart, GradientGreenEnd),
                    modifier = Modifier.weight(1f), subtitle = if (pendingFees > 0) "$pendingFees months pending" else "All fees clear",
                    onClick = { onNavigate(3) })
                DashboardCard(title = "Upcoming Tests", value = "${tests.size}",
                    icon = Icons.Filled.Quiz, gradientColors = listOf(GradientSaffronStart, GradientSaffronEnd),
                    modifier = Modifier.weight(1f), subtitle = if (tests.isNotEmpty()) "Next: ${DateUtils.formatDate(tests.first().date)}" else "No tests",
                    onClick = { onNavigate(2) })
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
        // Latest marks
        if (latestResult != null) {
            item {
                val pct = (latestResult.marksObtained.toFloat() / latestResult.totalMarks * 100).toInt()
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = GreenSurface)) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Latest Score", style = MaterialTheme.typography.labelLarge.copy(color = LeafGreenDark))
                            Text("${latestResult.marksObtained}/${latestResult.totalMarks}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = LeafGreenDark))
                            if (latestResult.remarks.isNotBlank()) Text(latestResult.remarks, style = MaterialTheme.typography.bodySmall, color = LeafGreenDark.copy(0.7f))
                        }
                        StatusChip(text = "$pct%", color = when { pct >= 80 -> SuccessGreen; pct >= 50 -> WarningAmber; else -> ErrorRed })
                    }
                }
            }
        }
        // Recent notes
        if (notes.isNotEmpty()) {
            item { SectionHeader("Latest Notes", actionText = "See All") { onNavigate(1) } }
            items(notes.take(3)) { note ->
                InfoCard(title = note.title, subtitle = "${note.subject} • ${note.topic}", icon = Icons.Outlined.MenuBook, onClick = { onNavigate(1) })
            }
        }
        // Notifications
        if (notifications.isNotEmpty()) {
            item { SectionHeader("Notifications") }
            items(notifications.take(5)) { notif ->
                InfoCard(title = notif.title, subtitle = "${notif.body}\n${DateUtils.getRelativeTime(notif.createdAt)}",
                    icon = when (notif.type) { "FEE_REMINDER" -> Icons.Outlined.Payment; "NEW_NOTE" -> Icons.Outlined.LibraryBooks; "TEST_SCHEDULED" -> Icons.Outlined.EventNote; "MARKS_UPLOADED" -> Icons.Outlined.Grade; else -> Icons.Outlined.Notifications },
                    trailingContent = { if (!notif.isRead) Badge { Text("New") } })
            }
        }
    }
}
