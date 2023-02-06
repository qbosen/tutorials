
@Suppress("DSL_SCOPE_VIOLATION") //fix in gradle 8.1
plugins {
    id("java")
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(libs.javers.core)
    testImplementation(libs.bundles.junit.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.slf4j.simple)
}


tasks.getByName<Test>("test") {
    useJUnitPlatform()
}