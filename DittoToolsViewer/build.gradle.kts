// libs. will show an IDE error. This is a bug with Android Studio/IntelliJ. This issue is
// is tracked here: https://youtrack.jetbrains.com/issue/KTIJ-19369
// Workaround is to suppress the error until the issue linked above is fixed
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
}

extra["libraryGroupId"] = "live.ditto"
extra["libraryArtifactId"] = "dittotoolsviewer"
extra["libraryVersion"] = "1.0.0"

apply {
    from("${rootProject.projectDir}/gradle/deploy.gradle")
    from("${rootProject.projectDir}/gradle/android-common.gradle")
}

android {
    namespace = "live.ditto.dittotoolsviewer"
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.androidx.appcompat.appcompat)
    implementation(libs.material)

    implementation(platform(libs.androidx.compose.composeBom))
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.androidx.compose.ui.uiToolingPreview)
    implementation(libs.androidx.navigation.navigationCompose)
    implementation(libs.androidx.compose.material3.material3)

    implementation(libs.live.ditto.ditto)

    implementation(libs.live.ditto.databrowser)
    implementation(libs.live.ditto.exportlogs)
    implementation(libs.live.ditto.presenceviewer)
    implementation(project(":DittoExporter"))

    // TODO: Use maven version once this ticket is done
    implementation(project(":DittoHeartbeat"))

    // TODO: Use maven version once this ticket is done
    //       https://github.com/getditto/DittoAndroidTools/issues/31
    implementation(project(":DittoDiskUsage"))
    // implementation libs.live.ditto.diskusage

    implementation(libs.live.ditto.health)
    implementation(project(":DittoPresenceDegradationReporter"))
    implementation(project(":DittoHealthMetrics"))

    testImplementation(libs.junit.junit)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}