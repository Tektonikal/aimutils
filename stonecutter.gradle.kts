plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3"

stonecutter parameters {
    val isOrnithe = current.version == "1.8.9"
    replacements {
        string(current.parsed < "1.21.9" && !isOrnithe) {
            replace("window.handle()", "window.getWindow()")
        }
        string(current.parsed < "1.20.5" && !isOrnithe) {
            replace("Math.clamp(", "net.minecraft.util.Mth.clamp(")
        }
    }
}

tasks.register("buildAll") {
    group = "build"
    description = "Builds every registered version and collects the jars"
    dependsOn(stonecutter.tasks.named("buildAndCollect"))
}
