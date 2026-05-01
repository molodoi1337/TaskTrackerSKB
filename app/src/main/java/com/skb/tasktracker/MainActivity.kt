package com.skb.tasktracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skb.tasktracker.ui.navigation.Screen
import com.skb.tasktracker.ui.screens.assignees.AssigneeEditScreen
import com.skb.tasktracker.ui.screens.assignees.AssigneeListScreen
import com.skb.tasktracker.ui.screens.projects.ProjectEditScreen
import com.skb.tasktracker.ui.screens.projects.ProjectListScreen
import com.skb.tasktracker.ui.screens.tasks.TaskDetailScreen
import com.skb.tasktracker.ui.screens.tasks.TaskEditScreen
import com.skb.tasktracker.ui.screens.tasks.TaskListScreen
import com.skb.tasktracker.ui.theme.TaskTrackerTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* user choice — work continues regardless */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureNotificationPermission()
        setContent {
            TaskTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = Screen.TaskList.route) {
                        composable(Screen.TaskList.route) {
                            TaskListScreen(
                                onAddTask = { navController.navigate(Screen.TaskEdit.build()) },
                                onAddProject = { navController.navigate(Screen.ProjectEdit.build()) },
                                onOpenTask = { navController.navigate(Screen.TaskDetail.build(it)) },
                                onOpenProjects = { navController.navigate(Screen.Projects.route) },
                                onOpenAssignees = { navController.navigate(Screen.Assignees.route) }
                            )
                        }
                        composable(Screen.Projects.route) {
                            ProjectListScreen(
                                onBack = { navController.popBackStack() },
                                onAdd = { navController.navigate(Screen.ProjectEdit.build()) },
                                onEdit = { navController.navigate(Screen.ProjectEdit.build(it)) }
                            )
                        }
                        composable(Screen.Assignees.route) {
                            AssigneeListScreen(
                                onBack = { navController.popBackStack() },
                                onAdd = { navController.navigate(Screen.AssigneeEdit.build()) },
                                onEdit = { navController.navigate(Screen.AssigneeEdit.build(it)) }
                            )
                        }
                        composable(
                            route = Screen.TaskEdit.route,
                            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
                        ) { entry ->
                            val id = entry.arguments?.getLong("id") ?: -1L
                            TaskEditScreen(
                                taskId = id.takeIf { it > 0 },
                                onBack = { navController.popBackStack() },
                                onAddAssignee = { navController.navigate(Screen.AssigneeEdit.build()) }
                            )
                        }
                        composable(
                            route = Screen.TaskDetail.route,
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { entry ->
                            val id = entry.arguments?.getLong("id") ?: return@composable
                            TaskDetailScreen(
                                taskId = id,
                                onBack = { navController.popBackStack() },
                                onEdit = { navController.navigate(Screen.TaskEdit.build(id)) }
                            )
                        }
                        composable(
                            route = Screen.ProjectEdit.route,
                            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
                        ) { entry ->
                            val id = entry.arguments?.getLong("id") ?: -1L
                            ProjectEditScreen(
                                projectId = id.takeIf { it > 0 },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = Screen.AssigneeEdit.route,
                            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
                        ) { entry ->
                            val id = entry.arguments?.getLong("id") ?: -1L
                            AssigneeEditScreen(
                                assigneeId = id.takeIf { it > 0 },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
