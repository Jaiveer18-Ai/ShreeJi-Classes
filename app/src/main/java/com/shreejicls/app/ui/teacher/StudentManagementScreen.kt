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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shreejicls.app.data.local.entity.UserEntity
import com.shreejicls.app.ui.components.*
import com.shreejicls.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentManagementScreen(vm: TeacherViewModel) {
    val students by vm.filteredStudents.collectAsState()
    val searchQuery by vm.searchQuery.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showCredentials by remember { mutableStateOf<Pair<String, String>?>(null) }
    var editStudent by remember { mutableStateOf<UserEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<UserEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { vm.searchStudents(it) },
            placeholder = { Text("Search students...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { vm.searchStudents("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = null)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronOrange)
        )

        // Student list
        if (students.isEmpty()) {
            EmptyState(Icons.Outlined.PersonOff, "No Students", "Add your first student to get started")
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                item {
                    Text("${students.size} Students", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                }
                items(students, key = { it.userId }) { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(shape = RoundedCornerShape(14.dp), color = SaffronSurface) {
                                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                        Text(student.name.first().toString(), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = SaffronOrange))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text("${student.userId} • Class ${student.studentClass}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (student.phone.isNotBlank()) {
                                    Text("📱 ${student.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            IconButton(onClick = { editStudent = student }) { Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = InfoBlue) }
                            IconButton(onClick = { deleteTarget = student }) { Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = ErrorRed) }
                        }
                    }
                }
            }
        }
    }

    // FAB
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.padding(24.dp),
            containerColor = SaffronOrange,
            contentColor = androidx.compose.ui.graphics.Color.White
        ) { Icon(Icons.Filled.PersonAdd, contentDescription = "Add Student") }
    }

    // Add Student Dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var cls by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var parentPhone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Student", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = cls, onValueChange = { cls = it }, label = { Text("Class (e.g., 10th)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = parentPhone, onValueChange = { parentPhone = it }, label = { Text("Parent Phone (Optional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank() && cls.isNotBlank()) {
                        vm.addStudent(name, cls, phone, parentPhone) { id, pwd -> showCredentials = Pair(id, pwd) }
                        showAddDialog = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    // Credentials Dialog
    showCredentials?.let { (id, pwd) ->
        AlertDialog(
            onDismissRequest = { showCredentials = null },
            title = { Text("✅ Student Created!", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Share these credentials with the student:")
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = GreenSurface), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("User ID: $id", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = LeafGreenDark))
                            Text("Password: $pwd", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = LeafGreenDark))
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { showCredentials = null }, colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)) { Text("Done") } }
        )
    }

    // Delete Confirmation
    deleteTarget?.let { student ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Student?") },
            text = { Text("Are you sure you want to delete ${student.name}? This action cannot be undone.") },
            confirmButton = { Button(onClick = { vm.deleteStudent(student); deleteTarget = null }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }

    // Edit Dialog
    editStudent?.let { student ->
        var eName by remember { mutableStateOf(student.name) }
        var eCls by remember { mutableStateOf(student.studentClass) }
        var ePhone by remember { mutableStateOf(student.phone) }
        var ePPhone by remember { mutableStateOf(student.parentPhone) }

        AlertDialog(
            onDismissRequest = { editStudent = null },
            title = { Text("Edit Student", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = eName, onValueChange = { eName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = eCls, onValueChange = { eCls = it }, label = { Text("Class") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = ePhone, onValueChange = { ePhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = ePPhone, onValueChange = { ePPhone = it }, label = { Text("Parent Phone") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.updateStudent(student.copy(name = eName, studentClass = eCls, phone = ePhone, parentPhone = ePPhone))
                    editStudent = null
                }, colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editStudent = null }) { Text("Cancel") } }
        )
    }
}
