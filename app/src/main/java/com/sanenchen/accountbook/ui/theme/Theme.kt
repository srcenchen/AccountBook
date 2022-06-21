package com.sanenchen.accountbook.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kiwi.orbit.compose.ui.OrbitTheme
import kiwi.orbit.compose.ui.foundation.darkColors
import kiwi.orbit.compose.ui.foundation.lightColors

@Composable
fun AccountBookTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    OrbitTheme(colors = if(darkTheme) darkColors() else lightColors()) {
        rememberSystemUiController().setSystemBarsColor(OrbitTheme.colors.surface.main, !darkTheme)
        content()
    }
}