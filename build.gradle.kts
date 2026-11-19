import net.ornithemc.ploceus.api.PloceusGradleExtensionApi

plugins {
    id("dev.kikugie.loom-back-compat")
    id("net.fabricmc.fabric-loom-remap") apply false
    id("ploceus") version "1.19.2" apply false
}

val isOrnithe = sc.current.version == "1.8.9"
val ploceus = if (isOrnithe) {
    pluginManager.apply("net.fabricmc.fabric-loom-remap")
    pluginManager.apply("ploceus")
    configurations.configureEach { exclude(group = "org.lwjgl.lwjgl") }
    extensions.getByType<PloceusGradleExtensionApi>().apply { setIntermediaryGeneration(2) }
} else null

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = property("mod.id") as String

val requiredJava: JavaVersion = when {
    isOrnithe -> JavaVersion.VERSION_25
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

sourceSets.main {
    java {
        if (isOrnithe) exclude("tektonikal/aimutils/Client.java", "tektonikal/aimutils/Config.java", "tektonikal/aimutils/ConfigScreen.java", "tektonikal/aimutils/ModMenuIntegration.java", "tektonikal/aimutils/IxerisCompat.java", "tektonikal/aimutils/mixin/MouseMixin.java")
        else exclude("tektonikal/aimutils/legacy/**", "tektonikal/aimutils/mixin/legacy/**")
    }
}

repositories {
    if (isOrnithe) {
        maven("https://maven.ornithemc.net/releases") { name = "OrnitheMC" }
    } else {
        maven("https://maven.isxander.dev/releases") { name = "Xander Maven" }
        maven("https://maven.terraformersmc.com/releases") { name = "TerraformersMC" }
        maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") { name = "DevAuth" }
    }
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
    if (isOrnithe) {
        maven("https://repo.polyfrost.org/releases") { name = "Polyfrost Releases" }
        maven("https://repo.polyfrost.org/snapshots") { name = "Polyfrost Snapshots" }
        maven("https://maven.cloverclient.com/releases") { content { includeGroup("pl.tomgirl") } }
        exclusiveContent {
            forRepository { maven("https://repo.hypixel.net/repository/Hypixel/") { name = "Hypixel" } }
            filter { includeGroup("net.hypixel") }
        }
        google()
        maven("https://redirector.kotlinlang.org/maven/compose-dev") { content { includeGroupByRegex("org\\.jetbrains\\..*") } }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

    if (isOrnithe) {
        mappings(ploceus!!.layeredMappings {
            mappings("net.ornithemc:feather-gen2:${sc.current.version}+build.${property("deps.feather_build")}:v2") { containsUnpick() }
        })
        ploceus.dependOsl(property("deps.osl_version") as String)
        val oneconfig = property("deps.oneconfig") as String
        modImplementation("org.polyfrost.oneconfig:${sc.current.version}-ornithe:$oneconfig") {
            exclude(group = "net.fabricmc", module = "fabric-loader")
            exclude(group = "me.djtheredstoner") // DevAuth
        }
        for (module in arrayOf("commands", "config", "config-impl", "events", "internal", "ui", "utils", "hud", "notifications", "poly-compose")) {
            implementation("org.polyfrost.oneconfig:$module:$oneconfig")
        }
    } else {
        loomx.applyMojangMappings()
        modImplementation("dev.isxander:yet-another-config-lib:${property("deps.yacl")}")
        modImplementation("com.terraformersmc:modmenu:${property("deps.modmenu")}") { isTransitive = false }
        modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
        if (sc.current.parsed < "26.1") modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")
        if (sc.current.parsed < "26.3") compileOnly("maven.modrinth:ixeris:${property("deps.ixeris")}:api") { isTransitive = false }
        if (findProperty("ixeris") == sc.current.version) {
            modLocalRuntime("maven.modrinth:ixeris:${property("deps.ixeris").toString().substringBefore('+')}+${sc.current.version}-fabric") { isTransitive = false }
        }
    }
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1")
    }

    mods.register(property("mod.id") as String) {
        sourceSet(sourceSets.main.get())
    }

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run")
        jvmArguments.add("-Dmixin.debug.export=true")
        if (name == "client" && sc.current.parsed < "26.1" && !isOrnithe) jvmArguments.addAll("-Ddevauth.enabled=true", "-Ddevauth.account=main")
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        val props = mapOf(
            "version" to version.toString(),
            "minecraft" to sc.properties.get<String>("mod.mc_compat"),
        )
        inputs.properties(props)
        filesMatching("fabric.mod.json") { expand(props) }
        if (isOrnithe) {
            val oneconfig = project.property("deps.oneconfig") as String
            inputs.property("oneconfig", oneconfig)
            filesMatching("fabric.mod.json") {
                filter { line ->
                    line.takeUnless { "\"fabric-api\"" in it || "ModMenuIntegration" in it }
                        ?.replace("tektonikal.aimutils.Client", "tektonikal.aimutils.legacy.LegacyClient")
                        ?.replace("aimutils.mixins.json", "aimutils.legacy.mixins.json")
                        ?.replace("\"yet_another_config_lib_v3\": \"*\"", "\"oneconfigv1\": \">=$oneconfig\", \"osl-lifecycle-events\": \"*\"")
                }
            }
            exclude("aimutils.mixins.json")
        } else {
            exclude("aimutils.legacy.mixins.json")
        }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        inputs.property("java", mixinJava)
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    withType<Jar> {
        val name = project.property("mod.id")
        inputs.property("mod_id", name)
        from("../../LICENSE.txt") { rename { "$it-$name" } }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}
