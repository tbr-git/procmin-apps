/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java library project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.5/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    id("de.rwth.procmin.tb.java-library")
}

group = "${group}.core"

dependencies {
  implementation("org.apache.logging.log4j:log4j-core:2.22.0")
  implementation("org.apache.logging.log4j:log4j-api:2.22.0")
  
  // Data and EMD
  implementation(project(":data-emd"))

  // Handling XES logs
  implementation(group = "prom", name = "XESLite", version = "latest", configuration = "default") {
    exclude(group = "log4j", module = "log4j")
    exclude(group = "prom-libs", module = "jgrapht")
  }
  implementation("javax.xml.bind:jaxb-api:2.3.1")

  // Third party
  implementation(group = "com.google.guava", name = "guava", version = "31.1-jre")

  implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.14.0-rc1")
  implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-parameter-names", version = "2.14.0-rc1")
  implementation(group = "com.fasterxml.jackson.datatype", name = "jackson-datatype-jsr310", version = "2.14.0-rc1")
  implementation(group = "com.fasterxml.jackson.datatype", name = "jackson-datatype-jdk8", version = "2.14.0-rc1")

  implementation(group = "org.jgrapht", name = "jgrapht-core", version = "1.5.1")
}

