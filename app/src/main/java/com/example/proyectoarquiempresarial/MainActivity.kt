package com.example.proyectoarquiempresarial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.proyectoarquiempresarial.ui.theme.ProyectoArquiEmpresarialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as CollectIQApp).container
        setContent {
            ProyectoArquiEmpresarialTheme {
                CollectIqRoot(container)
            }
        }
    }
}
