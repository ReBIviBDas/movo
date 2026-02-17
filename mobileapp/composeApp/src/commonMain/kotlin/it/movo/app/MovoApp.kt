package it.movo.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import it.movo.app.data.auth.TokenManager
import it.movo.app.ui.navigation.MovoNavigation
import it.movo.app.ui.theme.MovoTheme
import org.koin.compose.koinInject

@Composable
fun MovoApp() {
    val tokenManager: TokenManager = koinInject()
    val isLoggedIn by tokenManager.isLoggedIn.collectAsState()

    MovoTheme {
        MovoNavigation(isLoggedIn = isLoggedIn)
    }
}
