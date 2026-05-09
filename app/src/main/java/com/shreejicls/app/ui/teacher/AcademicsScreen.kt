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
fun AcademicsScreen(vm: TeacherViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Notes", "Tests", "Doubts", "Results")
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface, contentColor = SaffronOrange) {
            tabs.forEachIndexed { i, t -> Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(t, fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal) }) }
        }
        when (selectedTab) {
            0 -> NotesTab(vm)
            1 -> TestsTab(vm)
            2 -> DoubtsTab(vm)
            3 -> ResultsTab(vm)
        }
    }
}

@Composable
private fun NotesTab(vm: TeacherViewModel) {
    val notes by vm.allNotes.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        if (notes.isEmpty()) EmptyState(Icons.Outlined.LibraryBooks, "No Notes", "Upload study material")
        else LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)) {
            items(notes) { note ->
                InfoCard(title = note.title, subtitle = "${note.subject} • ${note.topic} • ${DateUtils.getRelativeTime(note.uploadDate)}", icon = when (note.type) { "PDF" -> Icons.Outlined.PictureAsPdf; "IMAGE" -> Icons.Outlined.Image; "VIDEO" -> Icons.Outlined.VideoLibrary; else -> Icons.Outlined.Link },
                    trailingContent = { IconButton(onClick = { vm.deleteNote(note) }) { Icon(Icons.Outlined.Delete, null, tint = ErrorRed) } })
            }
        }
        FloatingActionButton(onClick = { showAdd = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = LeafGreen, contentColor = Color.White) { Icon(Icons.Filled.NoteAdd, null) }
    }
    if (showAdd) {
        var title by remember { mutableStateOf("") }; var subject by remember { mutableStateOf("") }; var topic by remember { mutableStateOf("") }; var type by remember { mutableStateOf("LINK") }; var content by remember { mutableStateOf("") }; var desc by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showAdd = false }, title = { Text("Upload Notes", fontWeight = FontWeight.Bold) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topic") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("LINK", "PDF", "VIDEO").forEach { t -> FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) }) } }
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("URL / Path") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 2)
            } },
            confirmButton = { Button(onClick = { if (title.isNotBlank() && subject.isNotBlank()) { vm.addNote(title, subject, topic, type, content, desc); showAdd = false } }, colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)) { Text("Upload") } },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } })
    }
}

@Composable
private fun TestsTab(vm: TeacherViewModel) {
    val tests by vm.allTests.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        if (tests.isEmpty()) EmptyState(Icons.Outlined.EventNote, "No Tests", "Schedule a test")
        else LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)) {
            items(tests) { test ->
                val isPast = test.date < System.currentTimeMillis()
                InfoCard(title = test.title, subtitle = "${test.subject} • ${DateUtils.formatDate(test.date)} at ${test.time}\nSyllabus: ${test.syllabus}", icon = Icons.Outlined.Assignment,
                    trailingContent = { Column(horizontalAlignment = Alignment.End) { StatusChip(text = if (isPast) "Completed" else "Upcoming", color = if (isPast) MediumGray else LeafGreen); Spacer(Modifier.height(4.dp)); IconButton(onClick = { vm.deleteTest(test) }) { Icon(Icons.Outlined.Delete, null, tint = ErrorRed) } } })
            }
        }
        FloatingActionButton(onClick = { showAdd = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = InfoBlue, contentColor = Color.White) { Icon(Icons.Filled.EventNote, null) }
    }
    if (showAdd) {
        var title by remember { mutableStateOf("") }; var subject by remember { mutableStateOf("") }; var time by remember { mutableStateOf("10:00 AM") }; var syllabus by remember { mutableStateOf("") }; var marks by remember { mutableStateOf("100") }
        AlertDialog(onDismissRequest = { showAdd = false }, title = { Text("Schedule Test", fontWeight = FontWeight.Bold) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Test Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = syllabus, onValueChange = { syllabus = it }, label = { Text("Syllabus") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 2)
                OutlinedTextField(value = marks, onValueChange = { marks = it }, label = { Text("Total Marks") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            } },
            confirmButton = { Button(onClick = { if (title.isNotBlank() && subject.isNotBlank()) { vm.addTest(subject, title, System.currentTimeMillis() + 86400000L * 7, time, syllabus, marks.toIntOrNull() ?: 100); showAdd = false } }, colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)) { Text("Schedule") } },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } })
    }
}

@Composable
private fun DoubtsTab(vm: TeacherViewModel) {
    val doubts by vm.allDoubts.collectAsState()
    var replyTarget by remember { mutableStateOf<Long?>(null) }
    var replyText by remember { mutableStateOf("") }
    if (doubts.isEmpty()) EmptyState(Icons.Outlined.QuestionAnswer, "No Doubts", "Student doubts will appear here")
    else LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)) {
        items(doubts) { doubt ->
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text(doubt.studentName.ifBlank { doubt.studentId }, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)); Text(doubt.subject, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        StatusChip(text = doubt.status, color = if (doubt.status == "OPEN") WarningAmber else SuccessGreen)
                    }
                    Spacer(Modifier.height(8.dp)); Text(doubt.question, style = MaterialTheme.typography.bodyMedium)
                    if (doubt.reply != null) { Spacer(Modifier.height(8.dp)); Card(colors = CardDefaults.cardColors(containerColor = GreenSurface), shape = RoundedCornerShape(12.dp)) { Text("Reply: ${doubt.reply}", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, color = LeafGreenDark) } }
                    if (doubt.status == "OPEN") { Spacer(Modifier.height(8.dp)); Button(onClick = { replyTarget = doubt.doubtId; replyText = "" }, colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange), modifier = Modifier.height(36.dp)) { Text("Reply", style = MaterialTheme.typography.labelMedium) } }
                }
            }
        }
    }
    replyTarget?.let { id ->
        AlertDialog(onDismissRequest = { replyTarget = null }, title = { Text("Reply to Doubt") },
            text = { OutlinedTextField(value = replyText, onValueChange = { replyText = it }, label = { Text("Your Reply") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 3) },
            confirmButton = { Button(onClick = { if (replyText.isNotBlank()) { vm.replyToDoubt(id, replyText); replyTarget = null } }, colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)) { Text("Send") } },
            dismissButton = { TextButton(onClick = { replyTarget = null }) { Text("Cancel") } })
    }
}

@Composable
private fun ResultsTab(vm: TeacherViewModel) {
    val results by vm.allResults.collectAsState()
    val students by vm.students.collectAsState()
    val tests by vm.allTests.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        if (results.isEmpty()) EmptyState(Icons.Outlined.Assessment, "No Results", "Upload student marks")
        else LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)) {
            items(results) { r ->
                val sName = students.find { it.userId == r.studentId }?.name ?: r.studentId
                val tName = tests.find { it.testId == r.testId }?.title ?: "Test #${r.testId}"
                val pct = (r.marksObtained.toFloat() / r.totalMarks * 100).toInt()
                InfoCard(title = "$sName - $tName", subtitle = "${r.marksObtained}/${r.totalMarks} ($pct%) • ${r.remarks}", icon = Icons.Outlined.Grade,
                    trailingContent = { StatusChip(text = "$pct%", color = when { pct >= 80 -> SuccessGreen; pct >= 50 -> WarningAmber; else -> ErrorRed }) })
            }
        }
        FloatingActionButton(onClick = { showAdd = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = GoldAccent, contentColor = Color.White) { Icon(Icons.Filled.AddChart, null) }
    }
    if (showAdd) {
        var selStu by remember { mutableStateOf("") }; var selTest by remember { mutableStateOf(0L) }; var marks by remember { mutableStateOf("") }; var remarks by remember { mutableStateOf("") }; var expS by remember { mutableStateOf(false) }; var expT by remember { mutableStateOf(false) }
        AlertDialog(onDismissRequest = { showAdd = false }, title = { Text("Upload Marks", fontWeight = FontWeight.Bold) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(expanded = expS, onExpandedChange = { expS = it }) {
                    OutlinedTextField(value = students.find { it.userId == selStu }?.name ?: "", onValueChange = {}, readOnly = true, label = { Text("Student") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expS) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp))
                    ExposedDropdownMenu(expanded = expS, onDismissRequest = { expS = false }) { students.forEach { s -> DropdownMenuItem(text = { Text(s.name) }, onClick = { selStu = s.userId; expS = false }) } }
                }
                ExposedDropdownMenuBox(expanded = expT, onExpandedChange = { expT = it }) {
                    OutlinedTextField(value = tests.find { it.testId == selTest }?.title ?: "", onValueChange = {}, readOnly = true, label = { Text("Test") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expT) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp))
                    ExposedDropdownMenu(expanded = expT, onDismissRequest = { expT = false }) { tests.forEach { t -> DropdownMenuItem(text = { Text(t.title) }, onClick = { selTest = t.testId; expT = false }) } }
                }
                OutlinedTextField(value = marks, onValueChange = { marks = it }, label = { Text("Marks Obtained") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            } },
            confirmButton = { Button(onClick = { val m = marks.toIntOrNull(); val test = tests.find { it.testId == selTest }; if (m != null && selStu.isNotBlank() && test != null) { vm.addResult(selTest, selStu, m, test.totalMarks, remarks); showAdd = false } }, colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)) { Text("Upload") } },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } })
    }
}
