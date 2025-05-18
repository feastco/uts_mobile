plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.uts_a22202303006"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.uts_a22202303006"
        minSdk = 27
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //lottie
    implementation("com.github.LottieFiles:dotlottie-android:0.5.0")
    implementation("com.airbnb.android:lottie:6.6.0")

    //glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    implementation("com.google.code.gson:gson:2.10.1")

    //refresh layout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Toast Library
    implementation("com.github.GrenderG:Toasty:1.5.2")

    //material design
    implementation("com.google.android.material:material:1.9.0")

    //image slider
    implementation("com.github.denzcoskun:ImageSlideshow:0.1.2")

    //location
    implementation ("com.google.android.gms:play-services-location:21.0.1")

}