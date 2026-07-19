package com.redxai.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.redxai.ui.screens.builder.BuilderScreen
import com.redxai.ui.screens.chat.ChatListScreen
import com.redxai.ui.screens.chat.ChatScreen
import com.redxai.ui.screens.home.HomeScreen
import com.redxai.ui.screens.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val CHAT_LIST = "chats"
    const val CHAT = "chat/{chatId}"
    const val BUILDER = "builder"
    const val SETTINGS = "settings"

    fun chat(chatId: Long) = "chat/$chatId"
}

@Composable
fun RedxNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenChat = { navController.navigate(Routes.CHAT_LIST) },
                onOpenBuilder = { navController.navigate(Routes.BUILDER) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.CHAT_LIST) {
            ChatListScreen(
                onChatSelected = { chatId -> navController.navigate(Routes.chat(chatId)) },
                onNewChat = { chatId -> navController.navigate(Routes.chat(chatId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Routes.CHAT,
            arguments = listOf(navArgument("chatId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getLong("chatId") ?: return@composable
            ChatScreen(
                chatId = chatId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.BUILDER) {
            BuilderScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
