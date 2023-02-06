
@Suppress("DSL_SCOPE_VIOLATION") //fix in gradle 8.1
plugins {
    id("java")
    alias(libs.plugins.springboot)
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.springdoc.core)

    testImplementation(libs.bundles.junit.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.slf4j.simple)
    testImplementation(libs.spring.boot.starter.test)

}


tasks.getByName<Test>("test") {
    useJUnitPlatform()
}