plugins {
    id("com.android.application")
}

android {
    defaultConfig {
        applicationId = "com.hineeks.nexaproxy.plugin.naive"
    }
    namespace = "com.hineeks.nexaproxy.plugin.naive"
}

dependencies {
    implementation(project(":plugin:api"))
}

setupPlugin("naive")