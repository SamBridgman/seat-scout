plugins {
    id("com.android.application") version "9.0.1"
}

val demoCapture = providers.gradleProperty("demoCapture").isPresent
android {
    namespace = "edu.bu.seatscout.wear"
    compileSdk = 36
    defaultConfig {
        applicationId = "edu.bu.seatscout"
        minSdk = 30
        targetSdk = 36
        versionCode = 2
        versionName = "0.2"
    }
    sourceSets["main"].kotlin.srcDir("../shared/src/main/kotlin")
    sourceSets["main"].res.srcDir("../shared/src/main/res")
    if (demoCapture) {
        sourceSets["test"].kotlin.srcDir("src/demo/kotlin")
        testOptions.unitTests.isIncludeAndroidResources = true
    }
}

if (demoCapture) {
    dependencies {
        testImplementation("junit:junit:4.13.2")
        testImplementation("org.robolectric:robolectric:4.16.1")
    }
    tasks.withType<Test>().configureEach {
        maxHeapSize = "512m"
        systemProperty("seatscout.demo", rootProject.file("../Demo/frames").absolutePath)
        systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
    }
}

