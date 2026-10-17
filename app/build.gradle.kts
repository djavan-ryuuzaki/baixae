import com.android.build.api.variant.FilterConfiguration
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

/** Assinatura de release: lida de `keystore.properties` (fora do git); sem ele, o APK de release sai sem assinatura. */
val assinatura = rootProject.file("keystore.properties").takeIf { it.exists() }?.let { arquivo ->
    Properties().apply { arquivo.inputStream().use(::load) }
}

/** Uma APK por ABI; o versionCode de cada uma é `versionCode * 10 + índice`, para a de 64 bits vencer a de 32. */
val abis = listOf("armeabi-v7a", "arm64-v8a", "x86_64")

android {
    namespace = "br.com.djavanfernando.baixae"
    compileSdk = 37

    defaultConfig {
        applicationId = "br.com.djavanfernando.baixae"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val repositorioGithub = providers.gradleProperty("repositorioGithub").getOrElse("")
        buildConfigField("String", "REPOSITORIO_GITHUB", "\"$repositorioGithub\"")
    }

    signingConfigs {
        if (assinatura != null) {
            create("release") {
                storeFile = rootProject.file(assinatura.getProperty("storeFile"))
                storePassword = assinatura.getProperty("storePassword")
                keyAlias = assinatura.getProperty("keyAlias")
                keyPassword = assinatura.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include(*abis.toTypedArray())
            isUniversalApk = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

androidComponents {
    onVariants { variante ->
        variante.outputs.forEach { saida ->
            val abi = saida.filters.firstOrNull { it.filterType == FilterConfiguration.FilterType.ABI }?.identifier
            if (abi != null) saida.versionCode.set(saida.versionCode.get() * 10 + abis.indexOf(abi) + 1)
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.work.runtime.ktx)

    implementation(libs.youtubedl.library)
    implementation(libs.youtubedl.ffmpeg)

    implementation(libs.media3.transformer)
    implementation(libs.media3.effect)
    implementation(libs.media3.common)

    testImplementation(libs.junit)
    testImplementation(kotlin("test-junit"))
}
