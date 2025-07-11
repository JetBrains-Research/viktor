plugins {
    kotlin("jvm") version "2.2.0"

    id("maven-publish")
    id("signing")
    id("idea")
    id("me.champeau.jmh") version "0.7.2"
}

val kotlinVersion = "2.2.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
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
    systemProperty("java.library.path", "$buildDir/libs")
    jvmArgs("--add-modules", "jdk.incubator.vector")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("--add-modules", "jdk.incubator.vector"))
}

configure<org.gradle.plugins.ide.idea.model.IdeaModel> {
    module {
        name = "viktor"
    }
}

tasks.jar {
    archiveBaseName = "viktor"
    from("$buildDir/libs")
    exclude("*.jar")
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier = "sources"
    from(sourceSets.main.get().allSource)
}

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
