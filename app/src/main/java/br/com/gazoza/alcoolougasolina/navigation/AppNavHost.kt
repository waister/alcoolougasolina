package br.com.gazoza.alcoolougasolina.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.gazoza.alcoolougasolina.features.history.HistoryScreen
import br.com.gazoza.alcoolougasolina.features.main.MainScreen
import br.com.gazoza.alcoolougasolina.features.notifications.NotificationDetailsScreen
import br.com.gazoza.alcoolougasolina.features.notifications.NotificationsScreen
import br.com.gazoza.alcoolougasolina.features.start.StartScreen

object Routes {
    const val START = "start"
    const val MAIN = "main"
    const val HISTORY = "history"
    const val NOTIFICATIONS = "notifications"
    const val NOTIFICATION_DETAILS = "notification_details/{itemId}"

    fun notificationDetails(itemId: String): String = "notification_details/$itemId"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.START,
    initId: String = "",
    initType: String = "",
    initItemId: String = "",
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Routes.START) {
            StartScreen(
                id = initId,
                type = initType,
                itemId = initItemId,
                onNavigateToMain = { _ ->
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.START) { inclusive = true }
                    }
                },
                onNavigateToNotifications = {
                    navController.navigate(Routes.NOTIFICATIONS) {
                        popUpTo(Routes.START) { inclusive = true }
                    }
                },
                onNavigateToNotificationDetails = { itemId ->
                    navController.navigate(Routes.notificationDetails(itemId)) {
                        popUpTo(Routes.START) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() },
                onNotificationClick = { itemId ->
                    navController.navigate(Routes.notificationDetails(itemId))
                },
            )
        }

        composable(
            route = Routes.NOTIFICATION_DETAILS,
            arguments =
            listOf(
                navArgument("itemId") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            NotificationDetailsScreen(
                notificationId = itemId,
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
