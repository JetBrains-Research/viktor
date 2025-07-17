import org.gradle.internal.classpath.Instrumented.systemProperty
import java.util.Base64
import java.net.URL
import java.net.HttpURLConnection

plugins {
    kotlin("jvm") version "2.2.0"
    `maven-publish`
    signing
    id("me.champeau.jmh") version "0.7.2"
    id("de.undercouch.download") version "4.1.2"
    id("org.jetbrains.dokka") version "1.9.10"
}

val kotlinVersion = "2.2.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        javaParameters = true
        freeCompilerArgs.addAll("-Xjvm-default=all", "-Xadd-modules=java.base,jdk.incubator.vector")
    }
}

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileTestKotlin") {
    compilerOptions {
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        javaParameters = true
        freeCompilerArgs.addAll("-Xjvm-default=all", "-Xadd-modules=java.base,jdk.incubator.vector")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.jetbrains.bio:npy:0.3.5")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.slf4j:slf4j-api:2.0.17")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.slf4j:slf4j-log4j12:2.0.17")

    // JMH dependencies
    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

tasks.test {
    systemProperty("java.library.path", "${layout.buildDirectory.get().asFile}/libs")
    jvmArgs("--add-modules", "jdk.incubator.vector")
}

tasks.withType<JavaCompile>().configureEach {
    systemProperty("java.library.path", "${layout.buildDirectory.get().asFile}/libs")
    options.compilerArgs.addAll(listOf("--add-modules", "jdk.incubator.vector"))
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier = "sources"
    from(sourceSets.main.get().allSource)
}

val dokkaJavadocJar by tasks.creating(Jar::class) {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier = "javadoc"
}

tasks.dokkaJavadoc {
    outputDirectory.set(layout.buildDirectory.dir("dokka/javadoc"))
}


// ---------------------------------------------------------------------------------------------------------------------
// ---------- Publisher tool -------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "org.jetbrains.bio"
            artifactId = "viktor"
            from(components["java"])
            artifact(sourcesJar)
            artifact(dokkaJavadocJar)
            artifact(buildFile)

            pom {
                name = "viktor"
                packaging = "jar"
                description = "Efficient f64-only ndarray in Kotlin"
                url = "https://github.com/JetBrains-Research/viktor"

                scm {
                    connection = "scm:git:git@github.com:JetBrains-Research/viktor.git"
                    developerConnection = "scm:git:git@github.com:JetBrains-Research/viktor.git"
                    url = "https://github.com/JetBrains-Research/viktor"
                }

                licenses {
                    license {
                        name = "MIT License"
                        url = "https://github.com/JetBrains-Research/viktor/blob/master/LICENSE"
                    }
                }

                developers {
                    developer {
                        id = "dievsky"
                        name = "Aleksei Dievskii"
                        email = "alexey.dievsky@jetbrains.com"
                    }
                    developer {
                        id = "slebedev"
                        name = "Sergei Lebedev"
                        email = "sergei.a.lebedev@gmail.com"
                    }
                    developer {
                        id = "oshpynov"
                        name = "Oleg Shpynov"
                        email = "oleg.shpynov@gmail.com"
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "ossrh-staging-api"
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")

            credentials {
                username = findProperty("ossrhToken") as String?
                password = findProperty("ossrhTokenPassword") as String?
            }
        }
    }
}

tasks.named("publish") {
    doLast {
        val username = findProperty("ossrhToken") as String?
        val password = findProperty("ossrhTokenPassword") as String?

        if (username != null && password != null) {
            val url = "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/org.jetbrains"
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true

            // Set basic authentication
            val auth = "$username:$password"
            val encodedAuth = Base64.getEncoder().encodeToString(auth.toByteArray())
            connection.setRequestProperty("Authorization", "Basic $encodedAuth")

            // Set content type
            connection.setRequestProperty("Content-Type", "application/json")

            // Send empty body for POST request
            connection.outputStream.use { it.write("{}".toByteArray()) }

            // Get response
            val responseCode = connection.responseCode
            println("POST request to $url completed with response code: $responseCode")

            if (responseCode >= 400) {
                val errorStream = connection.errorStream
                val response = errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details available"
                println("Error response: $response")
                throw GradleException("Failed to complete publishing. Response code: $responseCode")
            } else {
                val inputStream = connection.inputStream
                val response = inputStream.bufferedReader().use { it.readText() }
                println("Success response: $response")
                println("Publishing completed successfully!")
            }
        } else {
            println("Warning: ossrhToken and ossrhTokenPassword properties are required to finish publishing")
        }
    }
}

configure<SigningExtension> {
    // multiline environment variables are not fun.
    val signingKey = findProperty("signingKey")?.toString()?.replace("\\n", "\n")
    val signingPassword = findProperty("signingPassword")?.toString()
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(the<PublishingExtension>().publications["mavenJava"])
    }
}

tasks.wrapper {
    gradleVersion = "8.5"
}


// ---------------------------------------------------------------------------------------------------------------------
// ---------- Benchmarking ---------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

// Configure JMH
jmh {
    // Set JMH version
    jmhVersion = "1.37"

    // Benchmark mode: Throughput/AverageTime/SampleTime/SingleShotTime/All
    benchmarkMode = listOf("thrpt")

    // Output time unit: NANOSECONDS/MICROSECONDS/MILLISECONDS/SECONDS
    timeUnit = "s"

    // Warmup iterations
    warmupIterations = 5

    // Measurement iterations
    iterations = 10

    // Number of forks
    fork = 2

    // JVM arguments
    jvmArgsAppend = listOf("--add-modules", "jdk.incubator.vector")

    // Include pattern for benchmarks
    includes = listOf(".*Benchmark.*")

    // Output format: text, csv, scsv, json, latex
    resultFormat = "csv"
}

// Create benchmark JAR task
tasks.register<Jar>("benchmarkJar") {
    dependsOn("jmhJar")
    archiveBaseName = "viktor-benchmark"
    from(tasks.named("jmhJar").get().outputs.files)
    manifest {
        attributes(mapOf(
            "Main-Class" to "org.openjdk.jmh.Main"
        ))
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
