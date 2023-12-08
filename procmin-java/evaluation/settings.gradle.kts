// == Define locations for build logic ==
pluginManagement {
    repositories {
        gradlePluginPortal() // if pluginManagement.repositories looks like this, it can be omitted as this is the default
    }
    includeBuild("../build-logic")
}

// == Define locations for components ==
dependencyResolutionManagement {
    repositories {
        mavenCentral()

        ivy {
              url = uri("https://github.com/promworkbench/")
              patternLayout {
                  artifact("[module]/raw/main/latestrelease/[artifact].[ext]")
                  ivy("[module]/raw/main/latestrelease/ivy.xml")
              }
        }

        ivy {
             url = uri("https://github.com/promworkbench/Releases/raw/main/Libraries/")
             patternLayout {
                 artifact("[module]/[revision]/[artifact]-[revision].[ext]")
                 artifact("[module]/[revision]/[artifact]_[revision].[ext]")
                 ivy("[module]/[revision]/ivy.xml")
             }
        }

        ivy {
             url = uri("https://github.com/promworkbench/Releases/raw/main/Packages/")
             patternLayout {
                 artifact("[module]/[revision]/[artifact]-[revision].[ext]")
                  ivy("[module]/[revision]/ivy.xml")
             }
        }
    }
}

includeBuild("../core")

// == Define the inner structure of this component ==
rootProject.name = "tb-evaluation"
include("bolt")
include("janusproxy")
include("sps-evaluation")
