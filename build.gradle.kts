import com.github.dockerjava.client.DockerException
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import se.transmode.gradle.plugins.docker.DockerTask

buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		val springBootVersion = "2.0.5.RELEASE"
		val kotlinVersion = "1.2.70"
		classpath("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
		classpath("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
		classpath("org.jetbrains.kotlin:kotlin-noarg:$kotlinVersion")
        classpath("se.transmode.gradle:gradle-docker:1.2")
	}
}

ext["spring_boot_version"] = "2.0.5.RELEASE"

apply {
	plugin("docker")
}

plugins {
	val kotlinVersion = "1.2.70"
	application
	kotlin("jvm") version kotlinVersion
	kotlin("plugin.jpa") version kotlinVersion
	kotlin("plugin.spring") version kotlinVersion
	eclipse
	id("org.springframework.boot") version "2.0.5.RELEASE"
	id("io.spring.dependency-management") version "1.0.6.RELEASE"
}

group = "com.valhallagame.valhalla"
version = "1.0"
setProperty("sourceCompatibility", JavaVersion.VERSION_1_8)
setProperty("mainClassName", "com.valhallagame.valhalla.recipeserviceserver.AppKt")

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"
compileKotlin.kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions.jvmTarget = "1.8"
compileTestKotlin.kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")

repositories {
	mavenCentral()
	maven {
		setUrl("https://artifactory.valhalla-game.com/libs-release")
	}
    maven {
        setUrl("https://artifactory.valhalla-game.com/libs-snapshot")
    }
	mavenLocal()
}

val test by tasks.getting(Test::class) {
	useJUnitPlatform()
}

dependencies {
	compile("org.springframework.boot:spring-boot-starter-data-jpa")
	compile("org.springframework.boot:spring-boot-starter-web")
	compile("com.fasterxml.jackson.module:jackson-module-kotlin")
	compile("org.flywaydb:flyway-core")
	compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	compile("org.jetbrains.kotlin:kotlin-reflect")
	compile("com.valhallagame.valhalla:character-service-client:1.0")
	compile("com.valhallagame.valhalla:wardrobe-service-client:1.0")
	compile("com.valhallagame.valhalla:recipe-service-client:1.1")
	compile("com.valhallagame.valhalla:currency-service-client:1.5")
	compile("com.valhallagame.valhalla:wardrobe-service-client:1.0")
	compile("com.valhallagame.valhalla:feat-service-client:1.0")
	compile("org.springframework.boot:spring-boot-starter-amqp")
	runtime("org.springframework.boot:spring-boot-devtools")
	runtime("org.postgresql:postgresql")

	testCompile("org.springframework.boot:spring-boot-starter-test")
	testCompile("com.h2database:h2:1.4.197")

	testCompile("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "junit")
	}
	testCompile("org.mockito:mockito-junit-jupiter:2.22.0")
	testCompile("org.flywaydb.flyway-test-extensions:flyway-spring-test:5.0.0")
	testCompile("org.skyscreamer:jsonassert:1.5.0")
	testImplementation("org.junit.jupiter:junit-jupiter-api")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

val copyJar = tasks.create<Exec>("copyJar") {
	executable = "sh"
	setArgs(listOf("-c", "mkdir -p build/docker && cp build/libs/${application.applicationName}-$version.jar build/docker/${application.applicationName}-$version.jar"))
}

tasks.create<DockerTask>("buildDocker") {
	dependsOn(copyJar)
	baseImage = "frolvlad/alpine-oraclejdk8:slim"
	registry = "https://registry.valhalla-game.com"
	if(project.hasProperty("tagVersion")) {
		tagVersion = project.properties["tagVersion"] as String
	}
	push = true
    tag = "registry.valhalla-game.com/saiaku/${application.applicationName}"
    entryPoint(listOf("java", "-Xmx256m", "-Xss512k", "-Xms32m", "-jar", "/${application.applicationName}-$version.jar"))
	addFile("${application.applicationName}-$version.jar", "/")
}
