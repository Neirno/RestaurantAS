plugins {
    kotlin("kapt")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.neirno.restaurantas"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.neirno.restaurantas"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3" // "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Настройки для Android Test
    testOptions {
        animationsDisabled = true
        unitTests {
            isReturnDefaultValues = true
        }
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.test.ext:junit-ktx:1.1.5")
    testImplementation("junit:junit:4.13.2")

    // To use the androidx.test.core APIs
    androidTestImplementation("androidx.test:core:1.5.0")
    // Kotlin extensions for androidx.test.core
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    // To use the JUnit Extension APIs
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    // Kotlin extensions for androidx.test.ext.junit
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.33.2-alpha")
    // test mockk
    val mockk_version = "1.13.8"
    testImplementation ("io.mockk:mockk:$mockk_version")

    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4") // Используйте актуальную версию



    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    // auth
    implementation("com.google.firebase:firebase-auth-ktx:22.2.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    // FCM
    implementation("com.google.firebase:firebase-messaging-ktx:23.3.0")
    // in app
    implementation("com.google.firebase:firebase-inappmessaging-display-ktx:20.4.0")
    // cloud firestore
    implementation("com.google.firebase:firebase-firestore-ktx:24.9.0")
    // remote config
    implementation("com.google.firebase:firebase-config-ktx:21.5.0")
    // Result Activity, but add later
    //implementation ("com.google.accompanist:accompanist-activity-result:0.18.0")

    // DATASTORE
    implementation("androidx.datastore:datastore-preferences:1.0.0")


    // hilt
    implementation("com.google.dagger:hilt-android:2.48")
    implementation ("androidx.hilt:hilt-navigation-compose:1.1.0-rc01")
    kapt("com.google.dagger:hilt-android-compiler:2.46.1")

    // icons
    implementation ("androidx.compose.material:material-icons-extended:1.5.4")

    // Навигация
    val nav_version = "2.7.4"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // Room
    val room_version = "2.6.0"
    //noinspection GradleDependency
    implementation ("androidx.room:room-ktx:$room_version")
    //implementation "androidx.room:room-runtime:$room_version"
    //noinspection KaptUsageInsteadOfKsp
    kapt("androidx.room:room-compiler:$room_version")

    // coil
    implementation ("io.coil-kt:coil-compose:1.4.0") // Проверьте актуальную версию

    //retrofit
    //implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    // GSON
    //implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    //implementation ("com.squareup.okhttp3:logging-interceptor:4.5.0")

    // MVI
    implementation("org.orbit-mvi:orbit-core:6.0.0")
    // or, if on Android:
    implementation("org.orbit-mvi:orbit-viewmodel:6.0.0")
    // If using Jetpack Compose include
    implementation("org.orbit-mvi:orbit-compose:6.0.0")
    // Tests
    testImplementation("org.orbit-mvi:orbit-test:6.0.0")
}