plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id ("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs")
}

android {
    namespace = "com.ali.whatsappplus"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ali.whatsappplus"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    // Gson
    implementation ("com.google.code.gson:gson:2.10.1")

    // CometChat
    implementation ("com.cometchat:chat-sdk-android:4.0.9")
    implementation ("com.cometchat:calls-sdk-android:4.0.6")
    // implementation ("com.cometchat:chat-uikit-android:4.+")

    // Hilt
    implementation ("com.google.dagger:hilt-android:2.51.1")
    implementation("androidx.activity:activity:1.10.1")
    kapt ("com.google.dagger:hilt-compiler:2.51.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-messaging")

    //Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    //Shimmer
    implementation ("com.facebook.shimmer:shimmer:0.5.0")

    // OKHTTP
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.activity:activity-ktx:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Jetpack Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.7")

    // Loupe
    implementation ("io.github.igreenwood.loupe:loupe:1.2.2")
    implementation ("io.github.igreenwood.loupe:extensions:1.0.1")

}