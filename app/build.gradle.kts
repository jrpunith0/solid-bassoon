import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

// Read production signing values without requiring them during debug builds.
// Release tasks are validated after Gradle has constructed the task graph.
val releaseKeystorePath = providers.environmentVariable("KEYSTORE_PATH")
val releaseStorePassword = providers.environmentVariable("STORE_PASSWORD")
val releaseKeyPassword = providers.environmentVariable("KEY_PASSWORD")

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    // Validate non-null local values before assigning to nullable Android DSL properties.
    val configuredApplicationId = providers.gradleProperty("APP_APPLICATION_ID").get()
    val configuredVersionCode = providers.gradleProperty("APP_VERSION_CODE").get().toIntOrNull()
      ?: error("APP_VERSION_CODE must be a positive integer.")
    val configuredVersionName = providers.gradleProperty("APP_VERSION_NAME").get()

    require(configuredApplicationId.matches(Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+"))) {
      "APP_APPLICATION_ID must be a valid, fully qualified Android application ID."
    }
    require(configuredVersionCode in 1..2_100_000_000) {
      "APP_VERSION_CODE must be between 1 and 2100000000."
    }
    require(configuredVersionName.isNotBlank()) { "APP_VERSION_NAME must not be blank." }

    applicationId = configuredApplicationId
    minSdk = 24
    targetSdk = 36
    versionCode = configuredVersionCode
    versionName = configuredVersionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      // No local/default keystore path: production signing must be explicit.
      releaseKeystorePath.orNull?.takeIf { it.isNotBlank() }?.let {
        storeFile = file(it)
      }
      storePassword = releaseStorePassword.orNull.orEmpty()
      keyAlias = "upload"
      keyPassword = releaseKeyPassword.orNull.orEmpty()
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Fail closed for every release variant, but do not require production secrets
// for debug builds or debug unit tests.
gradle.taskGraph.whenReady {
  val releaseTasks = allTasks.filter {
    it.project == project && it.name.contains("release", ignoreCase = true)
  }

  if (releaseTasks.isNotEmpty()) {
    val missing = buildList {
      if (releaseKeystorePath.orNull.isNullOrBlank()) add("KEYSTORE_PATH")
      if (releaseStorePassword.orNull.isNullOrBlank()) add("STORE_PASSWORD")
      if (releaseKeyPassword.orNull.isNullOrBlank()) add("KEY_PASSWORD")
    }

    check(missing.isEmpty()) {
      "Release signing requires the following environment variables: ${missing.joinToString()}"
    }

    val keystore = file(releaseKeystorePath.get())
    check(keystore.isFile && keystore.canRead()) {
      "Release keystore does not exist or is not readable at KEYSTORE_PATH."
    }
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future without deleting their configuration.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.androidx.media3.ui)
  implementation(libs.firebase.ai)
  implementation(libs.firebase.firestore)

  implementation(libs.firebase.auth)
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services)
  implementation(libs.googleid)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.firebase.appcheck.debug)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
