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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shreejicls.app.ui.components.*
import com.shreejicls.app.ui.theme.*
import com.shreejicls.app.util.DateUtils

@Composable
fun StudentDoubtScreen(vm: StudentViewModel, studentName: String) {
    val doubts by vm.myDoubts.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (doubts.isEmpty()) EmptyState(Icons.Outlined.QuestionAnswer, "No Doubts", "Ask your first doubt!")
        else LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)) {
            items(doubts) { doubt ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusChip(text = doubt.subject, color = SaffronOrange)
                            Spacer(Modifier.width(8.dp))
                            StatusChip(text = doubt.status, color = if (doubt.status == "OPEN") WarningAmber else SuccessGreen)
                            Spacer(Modifier.weight(1f))
                            Text(DateUtils.getRelativeTime(doubt.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(doubt.question, style = MaterialTheme.typography.bodyMedium)
                        if (doubt.reply != null) {
                            Spacer(Modifier.height(8.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = GreenSurface), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Teacher's Reply", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = LeafGreenDark))
                                    Text(doubt.reply, style = MaterialTheme.typography.bodySmall, color = LeafGreenDark)
                                }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(onClick = { showAdd = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = SaffronOrange, contentColor = Color.White) { Icon(Icons.Filled.Add, "Ask Doubt") }
    }

    if (showAdd) {
        var subject by remember { mutableStateOf("") }
        var question by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Ask a Doubt", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = question, onValueChange = { question = it }, label = { Text("Your Question") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 3)
                }
            },
            confirmButton = { Button(onClick = { if (subject.isNotBlank() && question.isNotBlank()) { vm.askDoubt(subject, question, studentName); showAdd = false } }, colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)) { Text("Submit") } },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }
}
