plugins {
    id("net.fabricmc.fabric-loom") version "1.19.3"
}

version = "${property("mod_version")}+${property("minecraft_version")}"
group = "dev.coinhider"
base.archivesName = "hypixel-skyblock-coin-hider"

// Minecraft 26.3 added a float partialTick parameter to EntityRenderDispatcher.shouldRender,
// so the render mixin has one source variant per signature.
val renderVariant = if (property("minecraft_version").toString().let { it.startsWith("26.1") || it.startsWith("26.2") }) "render_v1" else "render_v2"
sourceSets.main { java.srcDir("src/$renderVariant/java") }

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
    val props = mapOf("version" to project.version, "mc_compat" to project.property("mc_compat"))
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
}

tasks.jar {
    from("LICENSE")
}
