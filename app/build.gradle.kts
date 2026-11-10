
import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val forgeLocalProperties = Properties().apply {
    val archivo = rootProject.file("local.properties")

    if (archivo.exists()) {
        archivo.inputStream().use { input ->
            load(input)
        }
    }
}

fun forgeProperty(clave: String, defecto: String = ""): String {
    return (
            forgeLocalProperties.getProperty(clave)
                ?: System.getenv(clave)
                ?: defecto
            ).trim()
}

val apiBaseUrl = forgeProperty(
    "API_BASE_URL",
    "http://100.78.139.27:8000/api/"
).trimEnd('/') + "/"

android {
    namespace = "com.example.a3df_studio_app"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.a3df_studio_app"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"$apiBaseUrl\""
        )

        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${forgeProperty("GOOGLE_WEB_CLIENT_ID")}\""
        )
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf(
                    "androidx.**",
                    "kotlin.**",
                    "kotlinx.**"
                )
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Android Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation y Lifecycle
    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")

    // Imágenes
    implementation("io.coil-kt.coil3:coil-compose:3.1.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")

    // API y comunicaciones
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Google Authentication
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Debug
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
