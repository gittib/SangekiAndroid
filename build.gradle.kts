buildscript {
    val agp_version by extra("8.8.2")
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.8.2" apply false
    id("org.jetbrains.kotlin.android") version "2.2.21" apply false
    id("com.google.devtools.ksp") version "2.2.21-2.0.5" apply false
    kotlin("jvm") version "2.0.21" apply false
}
val sangekiDimension by extra("appli")
