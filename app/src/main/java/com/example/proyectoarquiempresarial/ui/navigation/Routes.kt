package com.example.proyectoarquiempresarial.ui.navigation

import android.net.Uri

object Routes {
    const val HOME = "home"
    const val INITIATE = "initiate"
    const val CASE = "case/{caseId}"
    const val STRATEGIES = "strategies"
    const val REGISTER = "register"
    const val PROFILE = "profile"

    fun case(caseId: String): String = "case/${Uri.encode(caseId)}"
}
