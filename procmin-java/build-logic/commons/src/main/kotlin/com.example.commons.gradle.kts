plugins {
    id("java")
}

group = "de.rwth.procmin.tb"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// Show logging
tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
        outputs.upToDateWhen {false}
    }
}
