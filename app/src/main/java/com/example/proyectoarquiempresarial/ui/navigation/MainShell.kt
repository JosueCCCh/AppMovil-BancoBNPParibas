package com.example.proyectoarquiempresarial.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyectoarquiempresarial.core.AppContainer
import com.example.proyectoarquiempresarial.core.Session
import com.example.proyectoarquiempresarial.core.UserRole
import com.example.proyectoarquiempresarial.ui.screens.case.CaseDetailScreen
import com.example.proyectoarquiempresarial.ui.screens.home.HomeScreen
import com.example.proyectoarquiempresarial.ui.screens.initiate.InitiateScreen
import com.example.proyectoarquiempresarial.ui.screens.profile.ProfileScreen
import com.example.proyectoarquiempresarial.ui.screens.strategies.RegisterStrategyScreen
import com.example.proyectoarquiempresarial.ui.screens.strategies.StrategiesScreen

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun MainShell(container: AppContainer, session: Session) {
    val navController = rememberNavController()
    val role = session.role
    val items = remember(role) {
        if (role == UserRole.GESTOR) {
            listOf(
                BottomItem(Routes.HOME, "Inicio", Icons.Default.Home),
                BottomItem(Routes.INITIATE, "Abrir caso", Icons.Default.Add),
                BottomItem(Routes.STRATEGIES, "Estrategias", Icons.Default.List),
                BottomItem(Routes.PROFILE, "Perfil", Icons.Default.Person)
            )
        } else {
            listOf(
                BottomItem(Routes.HOME, "Inicio", Icons.Default.Home),
                BottomItem(Routes.PROFILE, "Perfil", Icons.Default.Person)
            )
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                items.forEach { item ->
                    val selected = currentRoute == item.route ||
                        (item.route == Routes.HOME && currentRoute?.startsWith("case/") == true)
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    container = container,
                    role = role,
                    username = session.username,
                    onNewCase = { navController.navigate(Routes.INITIATE) },
                    onOpenCase = { caseId -> navController.navigate(Routes.case(caseId)) },
                    onStrategies = { navController.navigate(Routes.STRATEGIES) }
                )
            }

            composable(Routes.INITIATE) {
                InitiateScreen(
                    container = container,
                    onBack = { navController.popBackStack() },
                    onOpenCase = { caseId ->
                        navController.navigate(Routes.case(caseId)) {
                            popUpTo(Routes.HOME)
                        }
                    }
                )
            }

            composable(
                route = Routes.CASE,
                arguments = listOf(navArgument("caseId") { type = NavType.StringType })
            ) { entry ->
                val caseId = entry.arguments?.getString("caseId").orEmpty()
                CaseDetailScreen(
                    container = container,
                    caseId = caseId,
                    role = role,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.STRATEGIES) {
                StrategiesScreen(
                    container = container,
                    canRegister = role == UserRole.GESTOR,
                    onRegister = { navController.navigate(Routes.REGISTER) }
                )
            }

            composable(Routes.REGISTER) {
                RegisterStrategyScreen(
                    container = container,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    container = container,
                    session = session,
                    onRoleChanged = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
