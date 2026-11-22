import java.util.Properties

plugins {
    id("com.android.application") version "9.0.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10"
    id("com.google.devtools.ksp") version "2.3.4"
}

android {
    sourceSets["main"].kotlin.srcDir("../shared/src/main/kotlin")
    sourceSets["main"].res.srcDir("../shared/src/main/res")
    namespace = "edu.bu.seatscout"
    compileSdk = 36

    defaultConfig {
        applicationId = "edu.bu.seatscout"
        minSdk = 23
        targetSdk = 36
        versionCode = 3
        versionName = "0.3"
        val properties = Properties()
        val localConfig = rootProject.file("local.properties")
        if (localConfig.exists()) localConfig.inputStream().use(properties::load)
        fun configString(value: String) = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
        for (key in listOf("SUPABASE_URL", "SUPABASE_PUBLISHABLE_KEY")) {
            buildConfigField("String", key, configString(properties.getProperty(key, "").trim()))
        }
        val mapsKey = properties.getProperty("MAPS_API_KEY", "").trim()
        manifestPlaceholders["mapsKey"] = mapsKey
        buildConfigField("boolean", "HAS_MAPS_KEY", mapsKey.isNotEmpty().toString())
    }
    compileOptions { isCoreLibraryDesugaringEnabled = true }
    buildFeatures { compose = true; buildConfig = true }
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    implementation(platform("io.github.jan-tennert.supabase:bom:3.2.3"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.ktor:ktor-client-android:3.2.3")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.room:room-runtime:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    testImplementation("junit:junit:4.13.2")
}

// The desktop capture harness is opt-in so regular builds only resolve app dependencies.
if (providers.gradleProperty("demoCapture").isPresent) {
    android.sourceSets["test"].kotlin.directories += "src/demoCapture/kotlin"
    android.testOptions.unitTests.isIncludeAndroidResources = true
    dependencies {
        testImplementation("org.robolectric:robolectric:4.16.1")
        testImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
        testImplementation("androidx.compose.ui:ui-test-junit4")
    }
    tasks.withType<Test>().configureEach {
        maxHeapSize = "512m"
        systemProperty("seatscout.demo", rootProject.file("../Demo/frames").absolutePath)
        systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
    }
}
