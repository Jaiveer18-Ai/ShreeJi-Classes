package com.shreejicls.app.ui.teacher

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.shreejicls.app.ui.theme.*

data class TeacherTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

val teacherTabs = listOf(
    TeacherTab("Home", Icons.Outlined.Home, Icons.Filled.Home),
    TeacherTab("Students", Icons.Outlined.People, Icons.Filled.People),
    TeacherTab("Fees", Icons.Outlined.Receipt, Icons.Filled.Receipt),
    TeacherTab("Academics", Icons.Outlined.School, Icons.Filled.School)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherHome(
    vm: TeacherViewModel,
    userName: String,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SHREE JI CLASSES", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = SaffronOrange))
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Filled.MoreVert, "Menu") }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Change Password") }, onClick = { showMenu = false; onChangePassword() }, leadingIcon = { Icon(Icons.Outlined.Lock, null) })
                        DropdownMenuItem(text = { Text("Logout") }, onClick = { showMenu = false; onLogout() }, leadingIcon = { Icon(Icons.Outlined.Logout, null) })
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                teacherTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(if (selectedTab == index) tab.selectedIcon else tab.icon, tab.label) },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = SaffronOrange, indicatorColor = SaffronSurface)
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AnimatedContent(targetState = selectedTab, transitionSpec = { fadeIn() togetherWith fadeOut() }) { tab ->
                when (tab) {
                    0 -> TeacherDashboard(vm) { route ->
                        when (route) {
                            "students", "add_student" -> selectedTab = 1
                            "fees" -> selectedTab = 2
                            "notes", "tests", "doubts", "results" -> selectedTab = 3
                        }
                    }
                    1 -> StudentManagementScreen(vm)
                    2 -> FeesManagementScreen(vm)
                    3 -> AcademicsScreen(vm)
                }
            }
        }
    }
}
