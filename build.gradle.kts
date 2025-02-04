@file:Suppress("UnstableApiUsage")

import nl.javadude.gradle.plugins.license.License

plugins {
	id("com.github.hierynomus.license").version("0.16.1")
	alias(libs.plugins.quilt.loom)
	`maven-publish`
}

val modVersion: String by project
val mavenGroup: String by project
val modId: String by project

base.archivesName = modId
version = modVersion
group = mavenGroup

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.
	
	mavenCentral()
	
	maven {
		name = "ParchmentMC"
		url = uri("https://maven.parchmentmc.org")
	}
	
	// Mod Artifacts
	
	maven {
		name = "WTHIT Maven"
		url = uri("https://maven2.bai.lol")
		content {
			includeGroup("lol.bai")
			includeGroup("mcp.mobius.waila")
		}
	}
	
	maven {
		name = "TerraformersMC"
		url = uri("https://maven.terraformersmc.com/")
	}
	
	maven {
		name = "Modrinth"
		url = uri("https://api.modrinth.com/maven")
		content {
			includeGroup("maven.modrinth")
		}
	}
}

val modImplementationInclude by configurations.register("modImplementationInclude")

// All the dependencies are declared at gradle/libs.version.toml and referenced with "libs.<id>"
// See https://docs.gradle.org/current/userguide/platforms.html for information on how version catalogs work.
dependencies {
	minecraft(libs.minecraft)
	mappings(loom.layered {
		officialMojangMappings()
		parchment(libs.parchment)
	})
	
	// Loader
	modImplementation(libs.fabric.loader)
	
	// Libraries
	modImplementation(libs.fabric.api)
	
	// Mod Integrations
	modCompileOnly(libs.wthit)
	modCompileOnly(libs.wthit.api)
	modCompileOnly(libs.lucko.fabric.permissions) {
		exclude(group = "net.fabricmc.fabric-api")
		exclude(group = "net.fabricmc")
	}
	modCompileOnly(libs.sodium)
	
	modRuntimeOnly(libs.wthit)
	modRuntimeOnly(libs.modmenu) {
		exclude(group = "net.fabricmc.fabric-api")
		exclude(group = "net.fabricmc")
	}
	modRuntimeOnly(libs.luckperms)
	modRuntimeOnly(libs.lucko.fabric.permissions) {
		exclude(group = "net.fabricmc.fabric-api")
		exclude(group = "net.fabricmc")
	}
	modRuntimeOnly(libs.resource.explorer)
	modRuntimeOnly(libs.spark)
	modRuntimeOnly(libs.sodium)
}

configurations {
	runtimeClasspath {
		// remove duplicate fabric-loader
		exclude(group = "net.fabricmc", module = "fabric-loader")
	}
}

tasks.create("markImplInternal") {
	description = "Marks all implementation classes with @ApiStatus.Internal"
	fileTree("${project.projectDir}/src/main/java/gay/sylv/weird_wares/impl").matching {
		include("**/*.java")
	}.forEach {
		var contents = it.readText(charset = Charsets.UTF_8) // this project uses UTF-8
		val pattern = "^(?<whitespace>[^\\S\\n]*)(?<classKeywords>(private |public )*(static )*(final )*(class|record|interface)+ )".toRegex(RegexOption.MULTILINE)
		if (!contents.contains("@org.jetbrains.annotations.ApiStatus.Internal")) {
			contents = contents.replace(pattern) { match -> "${match.groups["whitespace"]?.value ?: ""}@org.jetbrains.annotations.ApiStatus.Internal\n${match.groups["whitespace"]?.value ?: ""}${match.groups["classKeywords"]!!.value}" }
			it.writeText(contents)
		}
	}
}

tasks.processResources {
	inputs.property("version", version)
	
	filesMatching("fabric.mod.json") {
		expand("group" to mavenGroup, "id" to modId, "version" to version)
	}
	
	filesMatching("**/lang/*.json") {
		expand("id" to modId)
	}
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	// Minecraft 1.21 upwards uses Java 21.
	options.release.set(21)
}

loom {
	accessWidenerPath.set(file("src/main/resources/$modId.accesswidener"))
}

fabricApi {
	configureDataGeneration()
}

java {
	// Still required by IDEs such as Eclipse and Visual Studio Code
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
	
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()
	
	// If this mod is going to be a library, then it should also generate Javadocs in order to aid with development.
	// Uncomment this line to generate them.
	// withJavadocJar()
}

// If you plan to use a different file for the license, don't forget to change the file name here!
tasks.withType<AbstractArchiveTask> {
	from("COPYING") {
		rename { "${it}_${modId}" }
	}
	
	from("COPYING.LESSER") {
		rename { "${it}_${modId}" }
	}
	
	from("LICENSE") {
		rename { "${it}_${modId}" }
	}
}

tasks.license.configure {
	mustRunAfter(tasks.licenseFormat)
}

tasks.build {
	dependsOn(tasks.licenseFormat)
	dependsOn(tasks.license)
	dependsOn(tasks["markImplInternal"])
	mustRunAfter(tasks["markImplInternal"])
}

tasks.withType<License> {
	header = file("LHEADER")
	exclude("**/*.json")
}

// Configure the maven publication
publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = mavenGroup
			artifactId = modId
			version = modVersion
			
			from(components["java"])
		}
	}
	
	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
		maven {
			url = uri("https://maven.muonmc.org/releases")
			credentials {
				username = System.getenv("MAVEN_USERNAME")
				password = System.getenv("MAVEN_PASSWORD")
			}
		}
	}
}
