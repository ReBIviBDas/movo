package it.movo.app

import androidx.compose.runtime.Composable
import it.movo.app.ui.theme.MovoTheme
import it.movo.app.ui.navigation.MovoNavigation

@Composable
fun MovoApp() {
    MovoTheme {
        MovoNavigation(isLoggedIn = false)
    }
}
