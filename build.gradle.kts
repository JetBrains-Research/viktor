import org.gradle.internal.classpath.Instrumented.systemProperty

plugins {
    kotlin("jvm") version "2.2.0"
    `maven-publish`
    signing
    id("me.champeau.jmh") version "0.7.2"
    id("de.undercouch.download") version "4.1.2"
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

tasks.jar {
    archiveBaseName = "viktor"
    from("${layout.buildDirectory.get().asFile}/libs")
    exclude("*.jar")
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier = "sources"
    from(sourceSets.main.get().allSource)
}


// ---------------------------------------------------------------------------------------------------------------------
// ---------- Signer tool ----------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

val jetSignUrl = "https://packages.jetbrains.team/maven/p/jcs/maven/com/jetbrains/jet-sign/45.64/jet-sign-45.64.jar"

fun doSignApp(): Boolean {
    return project.findProperty("signApp")?.toString()?.toBoolean() ?: false
}

val downloadJetSign by tasks.registering(de.undercouch.gradle.tasks.download.Download::class) {
    group = "jetsign"
    src(jetSignUrl)
    dest("${project.layout.buildDirectory.get().asFile}/tools/jet-sign.jar")
    overwrite(false)
}

val lazyDownloadJetSign by tasks.registering {
    group = "jetsign"
    description = "Downloads signing tool only if signing is enabled."
    if (doSignApp()) {
        dependsOn(downloadJetSign)
    }
}

fun signBinaries(paths: Array<String>) {
    println("==========================================================================================================")
    if (doSignApp()) {
        println("Signing: ${paths.joinToString()} ...")
        val cmdArgs = mutableListOf("${project.layout.buildDirectory.get().asFile}/tools/jet-sign.jar")
        cmdArgs.addAll(paths)
        project.javaexec {
            mainClass.set("-jar")
            args = cmdArgs
            systemProperties = mapOf(
                "content-type" to "application/x-jar",
                "jsign_replace" to "true"
            )
        }
    } else {
        println("Not signed: ${paths.joinToString()}")
    }
    println("==========================================================================================================")
}

val signJar by tasks.registering {
    group = "jetsign"
    description = "Sign application jar"
    dependsOn(tasks.build, lazyDownloadJetSign)
    val jarFile = "${layout.buildDirectory.get().asFile}/libs/viktor-${project.version}.jar"
    inputs.file(jarFile)
    outputs.file(jarFile)
    doLast {
        signBinaries(arrayOf(jarFile))
    }
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
            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)

            credentials {
                username = findProperty("ossrhUsername") as String?
                password = findProperty("ossrhPassword") as String?
            }
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

