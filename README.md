# CollectIQ — App móvil BIAN Collections

Aplicación Android nativa para el Service Domain **BIAN Collections** de CollectIQ (BNP Paribas), conectada al backend serverless desplegado en AWS: **Amazon Cognito + Amazon API Gateway + AWS Lambda + Amazon DynamoDB**.

La app consume las mismas rutas BIAN certificadas en Postman y autentica contra el User Pool de Cognito. No contiene credenciales de AWS ni secretos embebidos.

---

## 1. Entorno AWS utilizado

| Parámetro | Valor |
|---|---|
| Región | `us-east-2` |
| API base | `https://neifkj4g7a.execute-api.us-east-2.amazonaws.com/dev` |
| User Pool | `us-east-2_m314C7F5p` |
| App Client | `j0ep9adar6t42hp3h4tsde058` (sin client secret) |
| Autorizador | `cognito-collections-authorizer` |
| Cabecera | `Authorization: <IdToken>` (token crudo, sin `Bearer`) |
| Usuario de prueba | `gestor1` |
| Flujo habilitado | `ALLOW_USER_PASSWORD_AUTH` |

> La URL base se puede cambiar en tiempo de ejecución desde **Login → Configuración del servidor** o desde **Perfil → Servicio de cobranzas**.

---

## 2. Arquitectura de la app

```text
Compose UI (Material 3)
        ↓
ViewModel (StateFlow)
        ↓
AuthRepository / CollectionsRepository
        ↓
OkHttp + org.json
   ├── Amazon Cognito (InitiateAuth / RespondToAuthChallenge / Refresh)
   └── API Gateway (5 rutas BIAN)
        ↓
SessionStore cifrado (AES-GCM + Android Keystore)
```

Decisiones:

- Android nativo con Kotlin + Jetpack Compose. Sin Kotlin Multiplatform.
- Sin Amplify, sin AWS SDK y sin Retrofit: llamadas HTTPS directas con OkHttp, igual que la colección Postman validada.
- MVVM + Repository, navegación con Navigation Compose.
- Sesión persistida y cifrada; refresh automático antes de que expire el IdToken (60 min).
- Reintento único ante `401`; si el refresh falla, se cierra la sesión.

---

## 3. Roles

| Rol | Funcionalidades |
|---|---|
| **Gestor** | Inicio con KPIs, abrir caso, consultar caso, actualizar estado, listar y registrar estrategias, perfil |
| **Cliente** | Inicio, consultar su caso por `caseId`, registrar promesa de pago, perfil |

El rol es una **experiencia de uso** sobre el mismo usuario de Cognito; no modifica permisos del backend. Se elige al iniciar sesión y se puede cambiar en Perfil.

---

## 4. Pantallas

1. **Login** — usuario/contraseña, selector de rol, manejo de `NEW_PASSWORD_REQUIRED`, configuración de URL.
2. **Inicio** — saludo, KPIs, accesos rápidos y casos recientes.
3. **Abrir caso (Initiate)** — formulario validado y resultado con tramo, acción, moratorio y total.
4. **Detalle del caso** — atributos, resumen financiero y cambio de estado (`IN_PROGRESS`, `PROMISE`, `PAID`, `WRITTEN_OFF`).
5. **Estrategias** — listado de tramos activos.
6. **Registrar estrategia** — alta de un nuevo tramo con validaciones.
7. **Perfil** — usuario, rol, vigencia del token, URL del servicio, prueba de conexión y logout.

---

## 5. Mapa de endpoints

| Operación | Método y ruta | Pantalla |
|---|---|---|
| Listar estrategias | `GET /v1/Collections/Strategy/Retrieve` | Inicio / Estrategias |
| Abrir caso | `POST /v1/Collections/Initiate` | Abrir caso |
| Consultar caso | `GET /v1/Collections/{caseid}/Retrieve` | Detalle |
| Actualizar estado | `PUT /v1/Collections/{caseid}/Update` | Detalle |
| Registrar estrategia | `POST /v1/Collections/Strategy/Register` | Registrar estrategia |

### Manejo de errores

| HTTP | Comportamiento en la app |
|---|---|
| `200 / 201` | Se muestra el resultado y se guarda el caso en “recientes” |
| `400` | Mensaje de validación devuelto por Lambda |
| `401` | Refresh del token y reintento; si falla, logout |
| `404` | “No se encontró el caso solicitado” |
| `409` | “El caso ya no está en estado OPEN” / estrategia duplicada |
| Error de red | Aviso de conexión con opción de reintentar |

---

## 6. Requisitos

- Android Studio con SDK 37 instalado.
- JDK 21.
- Dispositivo o emulador con Android 7.0 (API 24) o superior.
- Conexión a internet.

---

## 7. Cómo ejecutar

1. Abrir la carpeta del proyecto en Android Studio.
2. Esperar la sincronización de Gradle.
3. Seleccionar un emulador o dispositivo.
4. Ejecutar `app`.
5. Iniciar sesión con el usuario de Cognito (`gestor1` y su contraseña definitiva).

### Línea de comandos

```powershell
.\gradlew :app:assembleDebug
.\gradlew :app:testDebugUnitTest
```

APK generado:

```text
app\build\outputs\apk\debug\app-debug.apk
```

---

## 8. Configuración

Los valores por defecto están en `app/build.gradle.kts` como `buildConfigField`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://neifkj4g7a.execute-api.us-east-2.amazonaws.com/dev\"")
buildConfigField("String", "COGNITO_REGION", "\"us-east-2\"")
buildConfigField("String", "COGNITO_CLIENT_ID", "\"j0ep9adar6t42hp3h4tsde058\"")
buildConfigField("String", "COGNITO_USER_POOL_ID", "\"us-east-2_m314C7F5p\"")
```

La URL de API Gateway también se puede cambiar desde la app; se guarda en `SharedPreferences` y se valida que empiece con `https://`.

---

## 9. Guion de prueba contra AWS

| Caso | Pasos en la app | Resultado esperado |
|---|---|---|
| CP-01 Login | Ingresar `gestor1` + contraseña | Home del rol seleccionado |
| CP-02 Estrategias | Inicio → Estrategias | Lista de estrategias activas (3–4) |
| CP-03 Initiate | Abrir caso: `ACC-00017`, `PARTY-000123`, PEN, 1000, 45 días | `201`, STR-002, moratorio 40, total 1040, OPEN |
| CP-04 Retrieve | Tocar el caso reciente o ingresar `caseId` | Detalle completo del expediente |
| CP-05A Update | Detalle → estado `PROMISE` → Actualizar | `200`, estado PROMISE |
| CP-05B Update repetido | Intentar actualizar otra vez | `409`: el caso ya no está OPEN |
| CP-06 Register | Estrategias → Nueva → `STR-005` / 121–9999 / LEGAL / 10 % | `201` o `409` si ya existe |
| CP-07 Validación | Abrir caso sin monto | Error local y/o `400` con `overdueAmount` |
| CP-08 Sin token | Cerrar sesión / invocar API sin token | `401 Unauthorized` |
| CP-09 Token inválido | Token adulterado en API Gateway/Postman | `401 Unauthorized` |
| CP-10 Automatización | Postman Collection Runner | 17/17 pruebas (evidencia del backend) |

> La app no incluye la contraseña. Se ingresa en pantalla y no se persiste.

---

## 10. Seguridad

- El **IdToken se envía crudo** en `Authorization`, como exige el autorizador de Cognito en API Gateway.
- Los tokens se guardan cifrados con **AES-GCM** y una clave no exportable del **Android Keystore**.
- `android:allowBackup="false"` para evitar copias de la sesión.
- Tráfico exclusivamente HTTPS (`cleartextTrafficPermitted="false"`).
- No se registran tokens en logs.
- El logout limpia la sesión persistida y en memoria.
- Para producción se recomienda añadir biometría, certificate pinning y grupos/roles reales en Cognito.

---

## 11. Estructura del proyecto

```text
app/src/main/java/com/example/proyectoarquiempresarial/
├─ CollectIQApp.kt
├─ CollectIqRoot.kt
├─ MainActivity.kt
├─ core/            # configuración, sesión, cifrado, validadores, formatos
├─ data/
│  ├─ auth/         # CognitoClient, AuthRepository
│  ├─ local/        # casos recientes
│  ├─ model/        # modelos y mappers JSON
│  ├─ remote/       # CollectionsApi, ApiResult
│  └─ repository/   # CollectionsRepository
└─ ui/
   ├─ components/   # componentes reutilizables
   ├─ navigation/   # rutas y MainShell
   ├─ screens/      # login, home, initiate, case, strategies, profile
   └─ theme/        # identidad visual CollectIQ
```

---

## 12. Limitaciones conocidas

- La API no expone un listado general de casos; por eso “Casos recientes” se guarda localmente (máximo 12).
- El cambio de rol es una experiencia de interfaz; ambos roles usan el mismo IdToken.
- El modo demo offline no está incluido en esta versión: la app requiere conexión con AWS.
- La fecha se presenta en UTC para que coincida exactamente con los valores almacenados en DynamoDB.

---

## 13. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `401 Unauthorized` al iniciar operaciones | Token vencido o inválido | Volver a iniciar sesión; la app intenta refresh automático |
| `401` inmediato tras login | Se envió `Bearer <token>` | La app ya envía el token crudo; verificar el autorizador |
| `Usuario o contraseña incorrectos` | Contraseña inicial o cambio pendiente | Completar el desafío `NEW_PASSWORD_REQUIRED` |
| `No se pudo conectar` | Sin internet, URL incorrecta o laboratorio apagado | Revisar URL en Perfil y probar conexión |
| `No se encontró el caso` | `caseId` incorrecto | Copiar el identificador generado por Initiate |
| `409` al actualizar | El caso ya no está OPEN | Consultar el estado actual; el backend es intencionalmente condicional |
| Estrategias vacías | No hay estrategias ACTIVE | Registrar una desde la app o DynamoDB |

---

## 14. Evidencias sugeridas para la entrega

- Login exitoso con `gestor1`.
- Inicio con KPIs y estrategias activas.
- Initiate con STR-002, moratorio 40 y total 1040.
- Detalle del caso y actualización a PROMISE.
- Segundo intento de actualización con mensaje de conflicto.
- Registro de estrategia.
- Perfil con prueba de conexión y vigencia del token.
- Captura de `assembleDebug` y `testDebugUnitTest` en verde.

---

**Proyecto de Arquitectura Empresarial · Universidad Ricardo Palma · 2026-II**
