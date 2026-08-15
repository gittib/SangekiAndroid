import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "work.boardgame.sangeki_rooper"
    compileSdk = 37

    defaultConfig {
        applicationId = "work.boardgame.sangeki_rooper"
        minSdk = 24
        targetSdk = 36
        versionCode = 34
        versionName = "1.7.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        val properties = Properties().apply {
            project.rootProject.file("keystore.properties").inputStream().use { fis ->
                load(fis)
            }
        }

        getByName("debug") {
            storeFile = file(properties.getProperty("debug.path"))
            storePassword = properties.getProperty("debug.storePassword")
            keyAlias = properties.getProperty("debug.alias")
            keyPassword = properties.getProperty("debug.keyPassword")
        }

        create("release") {
            storeFile = file(properties.getProperty("release.path"))
            storePassword = properties.getProperty("release.storePassword")
            keyAlias = properties.getProperty("release.alias")
            keyPassword = properties.getProperty("release.keyPassword")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        // layout関連の不要な警告をチェック対象外にする
        disable += "ContentDescription"
        disable += "RtlHardcoded"
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk.debugSymbolLevel = "FULL"
        }
    }
    flavorDimensions.add("appli")
    productFlavors {
        create("sangeki") {
            dimension = rootProject.extra["sangekiDimension"] as String
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // -------------------------------------------------------------------------
    // AndroidX / Compose / Lifecycle BOM
    //    ※ Lifecycle や Fragment などの AndroidX 関連を一括管理
    // -------------------------------------------------------------------------
    implementation(platform("androidx.compose:compose-bom:2026.08.00"))
    implementation("androidx.lifecycle:lifecycle-livedata-ktx")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx")

    // 単体で管理する AndroidX / UI 関連
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.9.0")

    // テスト関連
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    // -------------------------------------------------------------------------
    // Kotlin Coroutines BOM
    // -------------------------------------------------------------------------
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.11.0"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx2")

    // -------------------------------------------------------------------------
    // OkHttp BOM
    //    ※ OkHttp や MockWebServer などのバージョンを一括管理
    // -------------------------------------------------------------------------
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.4.0"))
    implementation("com.squareup.okhttp3:okhttp")

    // Retrofit 関連（※Retrofit自体は公式BOM非対応のため個別指定）
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:3.0.0")

    // その他のネットワーク・非同期関連
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    // -------------------------------------------------------------------------
    // Room Database
    // -------------------------------------------------------------------------
    implementation("androidx.room:room-runtime:2.8.2")
    ksp("androidx.room:room-compiler:2.8.2")

    // -------------------------------------------------------------------------
    // サードパーティ UI ライブラリ
    // -------------------------------------------------------------------------
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.makeramen:roundedimageview:2.3.0")

    // -------------------------------------------------------------------------
    // PDF Viewer
    // -------------------------------------------------------------------------
    implementation("io.github.afreakyelf:Pdf-Viewer:2.4.0")
}