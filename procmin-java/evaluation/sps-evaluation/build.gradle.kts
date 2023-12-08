val janusProxy by configurations.creating

plugins {
    id("de.rwth.procmin.tb.java-app")
}

group = "${group}.evaluation"

dependencies {
    // Logging
    implementation("org.apache.logging.log4j:log4j-core:2.22.0")
    implementation("org.apache.logging.log4j:log4j-api:2.22.0")
    // This dependency is used by the application.
    implementation("com.google.guava:guava:32.1.1-jre")

    // Command line arguments
    implementation(group = "net.sourceforge.argparse4j", name = "argparse4j", version = "0.9.0")

    // Parsing XES logs
    implementation(group = "prom", name = "XESLite", version = "latest", configuration = "default") {
      exclude(group = "log4j", module = "log4j")
      exclude(group = "prom-libs", module = "jgrapht")
    }
    
    ////////////////////////////////////////
    // Evaluator
    ////////////////////////////////////////
    // SPS Logic
    implementation("de.rwth.procmin.tb.core:sps-core")
    // Handling data and EMD solutions
    implementation("de.rwth.procmin.tb.core:data-emd")

    // Janus
    janusProxy (project(path = ":janusproxy", configuration = "shadow"))
    // Bolt
    implementation(project(":bolt"))
    implementation(group = "prom", name = "TransitionSystems", version = "latest", configuration = "default") {
      exclude(group = "log4j", module = "log4j")
      exclude(group = "prom-libs", module = "jgrapht")
    }
    
    ////////////////////////////////////////
    // Evaluation = PDFG Discovery 
    ////////////////////////////////////////
    implementation(group = "org.jgrapht", name = "jgrapht-core", version = "1.5.1")

    // Serialization
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.14.0-rc1")
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-parameter-names", version = "2.14.0-rc1")
    implementation(group = "com.fasterxml.jackson.datatype", name = "jackson-datatype-jsr310", version = "2.14.0-rc1")
    implementation(group = "com.fasterxml.jackson.datatype", name = "jackson-datatype-jdk8", version = "2.14.0-rc1")
}


tasks.register<Copy>("copyJanusProxy") {
    from(janusProxy)
    into(layout.buildDirectory.dir("janus"))
}

tasks.withType<JavaExec> {
    dependsOn(tasks.named<Copy>("copyJanusProxy"))
}

tasks.named("test") {
    dependsOn(tasks.named<Copy>("copyJanusProxy"))
}

application {
    mainClass = when(project.hasProperty("chooseMain")) {
        true -> project.property("chooseMain").toString()
        false -> "hfdd.evaluation.parametergs.IVFParamGSMain"
    }  
}

