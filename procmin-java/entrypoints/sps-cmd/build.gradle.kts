val janusProxy by configurations.creating

plugins {
    id("de.rwth.procmin.tb.java-app")
}

group = "${group}.entrypoints"

dependencies {
    // Logging
    implementation("org.apache.logging.log4j:log4j-core:2.22.0")
    implementation("org.apache.logging.log4j:log4j-api:2.22.0")

    // Command line arguments
    implementation(group = "net.sourceforge.argparse4j", name = "argparse4j", version = "0.9.0")
  
    ////////////////////////////////////////
    // Evaluator
    ////////////////////////////////////////
    // SPS Logic
    implementation("de.rwth.procmin.tb.core:sps-core")
    // Handling data and EMD solutions
    implementation("de.rwth.procmin.tb.core:data-emd")

}

application {
    mainClass = "de.rwth.processmining.tb.entrypoints.sps.MainSPS"
}

