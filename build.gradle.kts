plugins {
    kotlin("jvm") version "1.6.21"
    `java-gradle-plugin`
    `maven-publish`
}

group = "org.hshekhar"
version = "0.0.2-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

sourceSets.named("main") {
    java.srcDirs(
        "src/main/java",
        "src/main/kotlin",
        "${buildDir}/generated/source/proto/main/kotlin",
        "${buildDir}/generated/source/proto/main/grpc",
        "${buildDir}/generated/source/proto/main/grpckt"
    )
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.github.spullara.mustache.java:compiler:0.9.4")

    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        create("GenerateDynamo") {
            id = "${project.group}.plugin.example"
            displayName = "Example plugin"
            description = "Example plugin : Generates DynamoDB document from protobuf"
            implementationClass = "${project.group}.plugin.GeneratorPlugin"
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions{
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        //freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = false
    }
}
