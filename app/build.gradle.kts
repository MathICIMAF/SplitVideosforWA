plugins {
    id("com.android.application")
}

android {
    namespace = "com.amg.splitvideosforwa"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.amg.splitvideosforwa"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-process:2.6.1")
    implementation ("com.arthenica:mobile-ffmpeg-full-gpl:4.4")
    //implementation ("com.google.android.gms:play-services-ads:22.5.0")
    //implementation ("com.google.android.ump:user-messaging-platform:2.1.0")
    implementation("androidx.lifecycle:lifecycle-process:2.6.1")
    implementation("androidx.preference:preference-ktx:1.2.0")
    configurations.implementation {
        exclude(group = "org.jetbrains.kotlin", module= "kotlin-stdlib-jdk8")
    }
}