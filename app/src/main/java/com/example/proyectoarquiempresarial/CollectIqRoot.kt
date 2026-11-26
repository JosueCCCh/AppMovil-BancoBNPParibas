package com.example.proyectoarquiempresarial

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectoarquiempresarial.core.AppContainer
import com.example.proyectoarquiempresarial.ui.navigation.MainShell
import com.example.proyectoarquiempresarial.ui.screens.login.LoginScreen

@Composable
fun CollectIqRoot(container: AppContainer) {
    val session by container.authRepository.session.collectAsStateWithLifecycle()
    val currentSession = session
    val activity = LocalActivity.current

    // El login tiene cabecera verde oscura: iconos claros.
    // Las pantallas autenticadas son claras: iconos oscuros.
    SideEffect {
        val window = activity?.window ?: return@SideEffect
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
            currentSession != null
    }

    if (currentSession == null) {
        LoginScreen(container)
    } else {
        MainShell(container, currentSession)
    }
}
