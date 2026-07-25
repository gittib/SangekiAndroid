buildscript {
    val agp_version by extra("8.8.0")
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.8.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
    kotlin("jvm") version "2.0.21" apply false
}
val sangekiDimension by extra("appli")
