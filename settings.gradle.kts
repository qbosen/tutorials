rootProject.name = "tutorials"
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
include(
    ":libraries",
    ":spring-web:spring-requestparam-object"
)