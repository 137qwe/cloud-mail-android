package com.cloudmail.app.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.cloudmail.app.data.remote.dto.EmailDto
import com.cloudmail.app.ui.screens.InboxScreen
import com.cloudmail.app.ui.screens.ProfileScreen
import com.cloudmail.app.ui.screens.StarScreen
import com.cloudmail.app.util.GsonHolder

/**
 * 主界面：底部导航（收件 / 星标 / 我的）。
 */
@Composable
fun MainScaffold(
    onOpenEmail: (EmailDto) -> Unit,
    onOpenCompose: () -> Unit,
    onOpenAccounts: () -> Unit,
    onLogout: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.Email, contentDescription = "收件") },
                    label = { Text("收件") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.Star, contentDescription = "星标") },
                    label = { Text("星标") }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "我的") },
                    label = { Text("我的") }
                )
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (tab) {
                0 -> InboxScreen(
                    onOpenEmail = onOpenEmail,
                    onOpenCompose = onOpenCompose,
                    onOpenAccounts = onOpenAccounts
                )
                1 -> StarScreen(onOpenEmail = onOpenEmail)
                2 -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}

/**
 * EmailDto 转导航 JSON 参数（Uri 编码）。
 */
fun emailNavArg(email: EmailDto): String = Uri.encode(GsonHolder.gson.toJson(email))
