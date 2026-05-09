package com.shreejicls.app.ui.teacher

import androidx.compose.animation.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboard(
    vm: TeacherViewModel,
    onNavigate: (String) -> Unit
) {
    val studentCount by vm.studentCount.collectAsState()
    val pendingFees by vm.pendingFeesCount.collectAsState()
    val upcomingTests by vm.upcomingTestsCount.collectAsState()
    val openDoubts by vm.openDoubtsCount.collectAsState()
    val recentDoubts by vm.allDoubts.collectAsState()
    val recentTests by vm.upcomingTests.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            // Header
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Good Morning! 🙏", style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                Text("Teacher Dashboard", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
            }
        }

        // Stats cards
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardCard(
                    title = "Students", value = "$studentCount", icon = Icons.Filled.People,
                    gradientColors = listOf(GradientSaffronStart, GradientSaffronEnd),
                    modifier = Modifier.weight(1f), onClick = { onNavigate("students") }
                )
                DashboardCard(
                    title = "Pending Fees", value = "$pendingFees", icon = Icons.Filled.CurrencyRupee,
                    gradientColors = listOf(ErrorRed.copy(alpha = 0.9f), ErrorRed),
                    modifier = Modifier.weight(1f), subtitle = if (pendingFees > 0) "Action needed!" else "All clear",
                    onClick = { onNavigate("fees") }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardCard(
                    title = "Upcoming Tests", value = "$upcomingTests", icon = Icons.Filled.Quiz,
                    gradientColors = listOf(GradientGreenStart, GradientGreenEnd),
                    modifier = Modifier.weight(1f), onClick = { onNavigate("tests") }
                )
                DashboardCard(
                    title = "Open Doubts", value = "$openDoubts", icon = Icons.Filled.QuestionAnswer,
                    gradientColors = listOf(GradientGoldStart, GradientGoldEnd),
                    modifier = Modifier.weight(1f), onClick = { onNavigate("doubts") }
                )
            }
        }

        // Quick actions
        item {
            SectionHeader(title = "Quick Actions")
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                QuickActionButton("Add Student", Icons.Filled.PersonAdd, SaffronOrange) { onNavigate("add_student") }
                QuickActionButton("Upload Notes", Icons.Filled.NoteAdd, LeafGreen) { onNavigate("notes") }
                QuickActionButton("Schedule Test", Icons.Filled.EventNote, InfoBlue) { onNavigate("tests") }
                QuickActionButton("Add Fees", Icons.Filled.Receipt, GoldAccent) { onNavigate("fees") }
            }
        }

        // Upcoming tests
        if (recentTests.isNotEmpty()) {
            item { SectionHeader(title = "Upcoming Tests", actionText = "See All") { onNavigate("tests") } }
            items(recentTests.take(3)) { test ->
                InfoCard(
                    title = test.title,
                    subtitle = "${test.subject} • ${DateUtils.formatDate(test.date)} at ${test.time}",
                    icon = Icons.Outlined.Assignment,
                    trailingContent = {
                        val days = DateUtils.daysUntil(test.date)
                        StatusChip(
                            text = if (days <= 1) "Tomorrow" else "In ${days}d",
                            color = if (days <= 2) ErrorRed else LeafGreen
                        )
                    }
                )
            }
        }

        // Recent doubts
        if (recentDoubts.isNotEmpty()) {
            item { SectionHeader(title = "Recent Doubts", actionText = "See All") { onNavigate("doubts") } }
            items(recentDoubts.take(3)) { doubt ->
                InfoCard(
                    title = "${doubt.studentName}: ${doubt.subject}",
                    subtitle = doubt.question,
                    icon = Icons.Outlined.HelpOutline,
                    trailingContent = {
                        StatusChip(
                            text = doubt.status,
                            color = if (doubt.status == "OPEN") WarningAmber else SuccessGreen
                        )
                    },
                    onClick = { onNavigate("doubts") }
                )
            }
        }
    }
}
