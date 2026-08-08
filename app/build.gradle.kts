import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.omeryol.akisgesture"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.omeryol.akisgesture"
        minSdk = 26
        targetSdk = 35
        versionCode = 56
        versionName = "1.5.1"



    }

    signingConfigs {
        create("release") {
            val keystorePropsFile = rootProject.file("keystore.properties")
            val keystoreProps = Properties()
            check(keystorePropsFile.exists()) {
                "Release signing requires local keystore.properties."
            }
            keystorePropsFile.inputStream().use { keystoreProps.load(it) }
            fun requiredSigningProperty(name: String): String =
                keystoreProps.getProperty(name)?.takeIf(String::isNotBlank)
                    ?: error("Release signing property $name is missing in keystore.properties.")

            val ksPath = requiredSigningProperty("RELEASE_KEYSTORE_PATH")
            val ksFile = file(ksPath)
            check(ksFile.exists()) {
                "Release keystore file does not exist: $ksPath"
            }
            storeFile = ksFile
            storePassword = requiredSigningProperty("RELEASE_STORE_PASSWORD")
            keyAlias = requiredSigningProperty("RELEASE_KEY_ALIAS")
            keyPassword = requiredSigningProperty("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.dynamicanimation.ktx)

    testImplementation("junit:junit:4.13.2")

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
