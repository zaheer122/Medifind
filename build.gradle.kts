plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android).version("2.0.0")
    alias(libs.plugins.google.gms.google.services)
}
android {
    namespace = "com.app.medifindfinal"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.app.medifindfinal"
        minSdk = 23
        targetSdk = 34
        versionCode = 5
        versionName = "1.2"

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
    kotlinOptions {
        jvmTarget = "11"
    }
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.play.services.base)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.fido)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.places)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.play.services.auth)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.volley)
    implementation(libs.play.services.maps.v1820)
    implementation(libs.play.services.maps)
    implementation(libs.okhttp)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation("com.google.android.libraries.places:places:4.2.0")
    implementation("com.google.android.material:material:1.11.0")
}

/*implementation(libs.play.services.fido)
implementation(libs.play.services.fido)
implementation(libs.play.services.fido)
implementation(libs.play.services.fido)
implementation(libs.play.services.fido)
implementation(libs.play.services.fido)
implementation(libs.play.services.fido)
    implementation(libs.okhttp)
*/