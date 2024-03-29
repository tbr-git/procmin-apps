/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java library project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.5/userguide/building_java_projects.html in the Gradle documentation.
 */
import de.undercouch.gradle.tasks.download.Download
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("de.rwth.procmin.tb.java-app")
    // Download
    id("de.undercouch.download") version "5.5.0"
    // Fat Jar
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "${group}.evaluation"

dependencies {
    // Janus
    implementation(files("libs/Janus.jar"))
}

application {
    mainClass = "org.processmining.spsevaluation.janusproxy.ProxyJanus"
}

val createLibDir by tasks.registering {
    doLast {
        mkdir("libs")
    }
}

// Download Janus
task<DefaultTask>("downloadLib") {
    val url = "https://raw.githubusercontent.com/Oneiroe/Janus/master/Janus.jar"
    val dest = File("libs.Janus.jar")
    task<Download>("download-task") {
        src(url)
        dest(dest)
        onlyIfModified(true)
    }
    dependsOn(createLibDir)
    dependsOn("download-task")
}

tasks.withType<JavaCompile>() {
    dependsOn(tasks.named<DefaultTask>("downloadLib"))
}

// Output to build/libs/shadow.jar
val bundleJanusProxy = tasks.named<ShadowJar>("shadowJar") { 
    archiveBaseName.set("proxyJanus")
    archiveClassifier.set("")
    archiveVersion.set("")
}

// The shadow configuration (added by the plugin) should be consumable)
//configurations {
//    shadow {
//        isCanBeConsumed = true
//        isCanBeResolved = true
//    }
//}

artifacts {
    add("shadow", bundleJanusProxy)
} 
