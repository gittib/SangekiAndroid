import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "work.boardgame.sangeki_rooper"
    compileSdk = 35

    defaultConfig {
        applicationId = "work.boardgame.sangeki_rooper"
        minSdk = 24
        targetSdk = 35
        versionCode = 26
        versionName = "1.5.2"

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

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    // Retrofit ////////////////////////////////////////////////////////////////////////////////////
    // picasso
    implementation("com.squareup.picasso:picasso:2.71828")
    // gson
    implementation("com.google.code.gson:gson:2.13.2")
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // retrofit2
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    // CallAdapter for RxJava
    implementation("com.squareup.retrofit2:adapter-rxjava2:3.0.0")
    // Explicitly install RxJava
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    // for Android
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    // Retrofit ここまで /////////////////////////////////////////////////////////////////////////////


    // inline-block的な、よしなに折り返すレイアウト
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    // 円形のImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")
    // 角丸のImageView
    implementation("com.makeramen:roundedimageview:2.3.0")

    // RoomDatabase ////////////////////////////////////////////////////////////////////////////////

    implementation("androidx.room:room-runtime:2.8.2")
    ksp("androidx.room:room-compiler:2.8.2")
    // RoomDatabase ここまで ////////////////////////////////////////////////////////////////////////

    // PDFビューワ
    implementation("io.github.afreakyelf:Pdf-Viewer:2.4.0")
}
