package com.cloudmail.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cloudmail.app.data.remote.dto.EmailDto
import com.cloudmail.app.data.remote.dto.EmailType
import com.cloudmail.app.ui.screens.AccountScreen
import com.cloudmail.app.ui.screens.ComposeScreen
import com.cloudmail.app.ui.screens.EmailDetailScreen
import com.cloudmail.app.ui.screens.LoginScreen
import com.cloudmail.app.ui.viewmodel.AuthViewModel
import com.cloudmail.app.ui.viewmodel.ComposePrefill
import com.cloudmail.app.ui.viewmodel.SessionObserverViewModel
import com.cloudmail.app.util.GsonHolder

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val ACCOUNTS = "accounts"
    const val EMAIL_DETAIL = "email_detail/{emailJson}"
    const val COMPOSE = "compose?prefill={prefill}"

    fun emailDetail(email: EmailDto): String = "email_detail/${emailNavArg(email)}"

    fun compose(prefill: ComposePrefill): String =
        "compose?prefill=${android.net.Uri.encode(GsonHolder.gson.toJson(prefill))}"
}

/**
 * 根导航：登录态决定起始页；全局 401 事件强制回登录页。
 */
@Composable
fun AppNavHost(
    authViewModel: AuthViewModel = hiltViewModel(),
    sessionViewModel: SessionObserverViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    // 401 会话失效 → 回登录页
    LaunchedEffect(Unit) {
        sessionViewModel.unauthorized.collect {
            sessionViewModel.forceLogout()
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authViewModel.isLoggedIn) Routes.MAIN else Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoggedIn = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScaffold(
                onOpenEmail = { email ->
                    navController.navigate(Routes.emailDetail(email))
                },
                onOpenCompose = {
                    navController.navigate(Routes.compose(ComposePrefill()))
                },
                onOpenAccounts = {
                    navController.navigate(Routes.ACCOUNTS)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.EMAIL_DETAIL,
            arguments = listOf(navArgument("emailJson") { type = NavType.StringType })
        ) { entry ->
            val emailJson = entry.arguments?.getString("emailJson").orEmpty()
            EmailDetailScreen(
                emailJson = emailJson,
                onBack = { navController.popBackStack() },
                onReply = { email ->
                    val replyTo = if (email.type == EmailType.RECEIVE) {
                        email.sendEmail.orEmpty()
                    } else {
                        email.toEmail.orEmpty()
                    }
                    val prefill = ComposePrefill(
                        accountId = email.accountId,
                        recipients = listOf(replyTo),
                        subject = "Re: ${email.subject.orEmpty()}",
                        content = quoteBody(email),
                        sendType = ComposePrefill.TYPE_REPLY,
                        replyEmailId = email.emailId
                    )
                    navController.navigate(Routes.compose(prefill))
                },
                onForward = { email ->
                    val prefill = ComposePrefill(
                        accountId = email.accountId,
                        recipients = emptyList(),
                        subject = "Fwd: ${email.subject.orEmpty()}",
                        content = quoteBody(email),
                        sendType = ComposePrefill.TYPE_FORWARD,
                        replyEmailId = email.emailId
                    )
                    navController.navigate(Routes.compose(prefill))
                },
                onDeleted = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.COMPOSE,
            arguments = listOf(navArgument("prefill") { type = NavType.StringType; defaultValue = "" })
        ) { entry ->
            val prefillJson = entry.arguments?.getString("prefill").orEmpty()
            ComposeScreen(
                prefillJson = prefillJson,
                onBack = { navController.popBackStack() },
                onSent = { navController.popBackStack() }
            )
        }

        composable(Routes.ACCOUNTS) {
            AccountScreen(onBack = { navController.popBackStack() })
        }
    }
}

/** 回复/转发引用原文 */
private fun quoteBody(email: EmailDto): String {
    val text = email.text?.takeIf { it.isNotBlank() } ?: email.content?.replace(Regex("<[^>]+>"), "") ?: ""
    return "> ${text.replace("\n", "\n> ")}"
}
