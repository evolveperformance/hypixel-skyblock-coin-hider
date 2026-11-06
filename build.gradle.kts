plugins {
    id("net.fabricmc.fabric-loom") version "1.19.3"
}

version = "${property("mod_version")}+${property("minecraft_version")}"
group = "dev.coinhider"
base.archivesName = "hypixel-skyblock-coin-hider"

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") { expand("version" to project.version) }
}

tasks.jar {
    from("LICENSE")
}
