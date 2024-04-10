plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.acade_mic"
    compileSdk = 34

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/ASL2.0")
        exclude("META-INF/INDEX.LIST")
    }

    defaultConfig {
        applicationId = "com.example.acade_mic"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.room:room-runtime:2.6.1");
    annotationProcessor("androidx.room:room-compiler:2.6.1");
    implementation("androidx.room:room-ktx:2.6.1");

    implementation("com.google.cloud:google-cloud-speech:4.36.0")
    implementation("io.grpc:grpc-okhttp:1.63.0")
    implementation("io.grpc:grpc-stub:1.63.0")

    implementation("com.google.apis:google-api-services-translate:v2-rev20170525-2.0.0")
    implementation("com.google.http-client:google-http-client-jackson2:1.44.1")

    implementation("com.google.ai.client.generativeai:generativeai:0.2.2")
    implementation("com.google.guava:guava:31.0.1-android")
    implementation("androidx.concurrent:concurrent-futures:1.1.0")
}