plugins {
    `java-library`
    groovy
    id("io.freefair.lombok") version "6.4.3"
}

group = "net.sghill.jenkins"

sourceSets {
    register("jenkins")
}

repositories {
    maven {
        url = uri("https://repo.jenkins-ci.org/public")
    }
}

dependencies {
    "jenkinsCompileOnly"("org.jenkins-ci.main:jenkins-core:2.332.3")

    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("info.debatty:java-string-similarity:2.0.0")

    runtimeOnly("org.slf4j:slf4j-simple:1.7.36")

    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.assertj:assertj-core:3.23.1")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

val fetchedDir = layout.buildDirectory.dir("fetched")
tasks.register<JavaExec>("fetch") {
    classpath = configurations["runtimeClasspath"] + sourceSets.main.get().output
    mainClass.set("net.sghill.jenkins.toolkit.FetchFailed")
    systemProperty("baseUrl", "http://localhost:8080")
    systemProperty("outDir", fetchedDir.get())
    systemProperty("script", layout.projectDirectory.file("src/jenkins/groovy/find-failed.groovy"))
}

tasks.register<JavaExec>("categorize") {
    classpath = configurations["runtimeClasspath"] + sourceSets.main.get().output
    mainClass.set("net.sghill.jenkins.toolkit.CategorizeFailures")
    systemProperty("inputDir", fetchedDir.get())
}
