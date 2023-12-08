dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "build-logic"
include("commons")
include("java-library")
include("java-app")
