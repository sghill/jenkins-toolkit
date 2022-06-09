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
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp.core)
    implementation("info.debatty:java-string-similarity:2.0.0")
    implementation(platform(libs.jackson.bom))
    implementation(libs.bundles.retrofit)

    runtimeOnly("org.slf4j:slf4j-simple:1.7.36")

    testImplementation(platform(testLibs.junit.bom))
    testImplementation(platform(testLibs.mockito.bom))
    testImplementation(testLibs.bundles.junit5)
    testImplementation(testLibs.okhttp.mockwebserver)
    testImplementation(libs.commons.io)

    testRuntimeOnly(testLibs.junit.engine)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-parameters"))
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
