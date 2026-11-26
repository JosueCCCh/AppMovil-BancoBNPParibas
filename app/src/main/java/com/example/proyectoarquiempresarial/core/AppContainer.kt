package com.example.proyectoarquiempresarial.core

import android.content.Context
import com.example.proyectoarquiempresarial.data.auth.AuthRepository
import com.example.proyectoarquiempresarial.data.auth.CognitoClient
import com.example.proyectoarquiempresarial.data.local.RecentCasesStore
import com.example.proyectoarquiempresarial.data.remote.CollectionsApi
import com.example.proyectoarquiempresarial.data.repository.CollectionsRepository
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    private val applicationContext = context.applicationContext

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(40, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    val configStore = ConfigStore(applicationContext)
    val sessionStore = SessionStore(applicationContext)
    val authRepository = AuthRepository(
        cognito = CognitoClient(httpClient),
        sessionStore = sessionStore,
        configStore = configStore
    )
    val recentCasesStore = RecentCasesStore(applicationContext)
    val collectionsRepository = CollectionsRepository(
        api = CollectionsApi(httpClient, configStore),
        auth = authRepository,
        recentCases = recentCasesStore
    )
}
