package com.shreejicls.app.ui.student

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

data class StudentTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

val studentTabs = listOf(
    StudentTab("Home", Icons.Outlined.Home, Icons.Filled.Home),
    StudentTab("Study", Icons.Outlined.MenuBook, Icons.Filled.MenuBook),
    StudentTab("Doubts", Icons.Outlined.HelpOutline, Icons.Filled.Help),
    StudentTab("Profile", Icons.Outlined.Person, Icons.Filled.Person)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHome(
    vm: StudentViewModel,
    studentName: String,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var studySubTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SHREE JI CLASSES", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = SaffronOrange)) },
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
                studentTabs.forEachIndexed { index, tab ->
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
                    0 -> StudentDashboard(vm, studentName) { navTab -> selectedTab = navTab }
                    1 -> {
                        Column {
                            TabRow(selectedTabIndex = studySubTab, containerColor = MaterialTheme.colorScheme.surface, contentColor = SaffronOrange) {
                                Tab(selected = studySubTab == 0, onClick = { studySubTab = 0 }, text = { Text("Notes") })
                                Tab(selected = studySubTab == 1, onClick = { studySubTab = 1 }, text = { Text("Tests") })
                            }
                            when (studySubTab) {
                                0 -> StudentNotesScreen(vm)
                                1 -> StudentTestsScreen(vm)
                            }
                        }
                    }
                    2 -> StudentDoubtScreen(vm, studentName)
                    3 -> {
                        Column {
                            var profileSubTab by remember { mutableIntStateOf(0) }
                            TabRow(selectedTabIndex = profileSubTab, containerColor = MaterialTheme.colorScheme.surface, contentColor = SaffronOrange) {
                                Tab(selected = profileSubTab == 0, onClick = { profileSubTab = 0 }, text = { Text("Fees") })
                                Tab(selected = profileSubTab == 1, onClick = { profileSubTab = 1 }, text = { Text("Progress") })
                            }
                            when (profileSubTab) {
                                0 -> StudentFeesScreen(vm)
                                1 -> StudentProgressScreen(vm)
                            }
                        }
                    }
                }
            }
        }
    }
}
