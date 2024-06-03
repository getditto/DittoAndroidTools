// libs. will show an IDE error. This is a bug with Android Studio/IntelliJ. This issue is
// is tracked here: https://youtrack.jetbrains.com/issue/KTIJ-19369
// Workaround is to suppress the error until the issue linked above is fixed
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
}

extra["libraryGroupId"] = "live.ditto"
extra["libraryArtifactId"]  = "dittotoolsviewer"
extra["libraryVersion"]  = "1.0.0"

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

    testImplementation(libs.junit.junit)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}