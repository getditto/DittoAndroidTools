plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
}

val libraryArtifactId by extra("ditto-tools-android")

apply(from = "${rootProject.projectDir}/gradle/deploy.gradle")
apply(from = "${rootProject.projectDir}/gradle/android-common.gradle")

android {
    namespace = "live.ditto.androidtools"
}

dependencies {
    implementation(libs.androidx.runtime.android)
    implementation(libs.core.ktx)
    implementation(libs.androidx.appcompat.appcompat)

    implementation(libs.androidx.compose.material3.material3)
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.androidx.compose.ui.uiTooling)
    implementation(libs.androidx.compose.ui.uiToolingPreview)
    implementation(libs.androidx.navigation.navigationCompose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.composeBom))
    implementation(libs.joda.time.joda.time)

    implementation(libs.material)
    implementation(libs.androidx.webkit)

    implementation(libs.live.ditto.ditto) {
        version {
            strictly("[4.5.0,)")
        }
    }

    testImplementation(libs.junit.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}