import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
}

android {
    namespace = "work.boardgame.sangeki_rooper"
    compileSdk = 34

    defaultConfig {
        applicationId = "work.boardgame.sangeki_rooper"
        minSdk = 24
        targetSdk = 34
        versionCode = 14
        versionName = "1.2.1"

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
        dataBinding = true
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
            isMinifyEnabled = false
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
            dimension = "appli"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Retrofit ////////////////////////////////////////////////////////////////////////////////////
    // picasso
    implementation("com.squareup.picasso:picasso:2.5.2")
    // gson
    implementation("com.google.code.gson:gson:2.9.0")
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    // retrofit2
    implementation("com.squareup.retrofit2:retrofit:2.3.0")
    implementation("com.squareup.retrofit2:converter-gson:2.3.0")
    // CallAdapter for RxJava
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.3.0")
    // Explicitly install RxJava
    implementation("io.reactivex.rxjava2:rxjava:2.1.10")
    // for Android
    implementation("io.reactivex.rxjava2:rxandroid:2.0.2")
    // Retrofit ここまで /////////////////////////////////////////////////////////////////////////////


    // inline-block的な、よしなに折り返すレイアウト
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    // 円形のImageView
    implementation("de.hdodenhof:circleimageview:2.2.0")
    // 角丸のImageView
    implementation("com.makeramen:roundedimageview:2.3.0")

    // RoomDatabase ////////////////////////////////////////////////////////////////////////////////
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    // RoomDatabase ここまで ////////////////////////////////////////////////////////////////////////

    // PDFビューワ
    implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")
}
