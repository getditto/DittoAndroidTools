plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.jreleaser)
}

val libraryArtifactId by extra("ditto-tools-android")

apply(from = "${rootProject.projectDir}/gradle/deploy.gradle")
apply(from = "${rootProject.projectDir}/gradle/android-common.gradle")

android {
    namespace = "live.ditto.tools"
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.androidx.appcompat.appcompat)

    implementation(libs.androidx.compose.material3.material3)
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.androidx.compose.ui.uiTooling)
    implementation(libs.androidx.compose.ui.uiToolingPreview)
    implementation(libs.androidx.navigation.navigationCompose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.composeBom))
    implementation(libs.androidx.compose.runtime.runtimeLivedata)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.joda.time.joda.time)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.material)
    implementation(libs.androidx.webkit)

    implementation(libs.live.ditto.ditto)

    testImplementation(libs.junit.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}