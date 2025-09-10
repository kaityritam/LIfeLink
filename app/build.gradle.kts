import com.android.build.api.dsl.Packaging
import java.util.Properties

// Function to read from local.properties file
fun getLocalProperty(key: String): String {
    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { input ->
            properties.load(input)
        }
    } else {
        throw GradleException("local.properties file not found")
    }
    return properties.getProperty(key) ?: throw GradleException("Property $key not found in local.properties")
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.lifelink.lifelink"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lifelink.lifelink"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "PROJECT_URL", "\"${getLocalProperty("PROJECT_URL")}\"")
        buildConfigField("String", "PROJECT_ANON_KEY", "\"${getLocalProperty("PROJECT_ANON_KEY")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }

    buildFeatures {
        viewBinding =true
        dataBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/versions/9/OSGI-INF/MANIFEST.MF",
                "META-INF/*.md",
                "META-INF/*.kotlin_module"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.recyclerview)
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation(libs.identity.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform("io.github.jan-tennert.supabase:bom:2.4.0"))

    // Supabase modules
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")

    // Ktor Android client
    implementation("io.ktor:ktor-client-okhttp:2.3.7")

    // Kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    // JSON to Kotlin data class converter

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    
}