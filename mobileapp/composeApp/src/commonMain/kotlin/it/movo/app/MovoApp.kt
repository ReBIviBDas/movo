package it.movo.app

import androidx.compose.runtime.Composable
import it.movo.app.ui.navigation.MovoNavigation
import it.movo.app.ui.theme.MovoTheme

@Composable
fun MovoApp() {
    MovoTheme {
        MovoNavigation(isLoggedIn = false)
    }
}
