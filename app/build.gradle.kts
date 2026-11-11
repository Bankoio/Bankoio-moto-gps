plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.jamenoy.connect"
    compileSdk = 34
    defaultConfig { applicationId = "com.jamenoy.connect"; minSdk = 24; targetSdk = 34; versionCode = 247; versionName = "2.47" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies { implementation("com.github.mik3y:usb-serial-for-android:3.7.3") }
