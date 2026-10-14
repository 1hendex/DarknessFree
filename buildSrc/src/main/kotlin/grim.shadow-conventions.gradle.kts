import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import versioning.BuildConfig

plugins {
    id("com.gradleup.shadow")
}

tasks.named<ShadowJar>("shadowJar") {
    minimize {
        // adventure's DataComponentValueConverter gson provider is only referenced via
        // ServiceLoader, so minimize() strips it and adventure's static init then throws
        // (ServiceConfigurationError) on enable. Keep the gson serializer's classes.
        exclude(dependency("net.kyori:adventure-text-serializer-gson:.*"))
        // slf4j-jdk14's provider is reached only through ServiceLoader, so minimize()
        // drops it and the shaded slf4j goes back to printing that it found no providers.
        exclude(dependency("org.slf4j:slf4j-jdk14:.*"))
    }
    archiveFileName = "${rootProject.name}-${project.name}-${rootProject.version}.jar"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    if (BuildConfig.relocate) {
        if (BuildConfig.shadePE) {
            relocate("io.github.retrooper.packetevents", "ac.darkness.darkness.shaded.io.github.retrooper.packetevents")
            relocate("com.github.retrooper.packetevents", "ac.darkness.darkness.shaded.com.github.retrooper.packetevents")
            relocate("net.kyori", "ac.darkness.darkness.shaded.kyori") // use PE's built-in adventure instead when not shading PE
        }
        relocate("club.minnced", "ac.darkness.darkness.shaded.discord-webhooks")
        relocate("org.slf4j", "ac.darkness.darkness.shaded.slf4j") // Required by HikariCP; slf4j-jdk14 ships beside it as the provider
        relocate("github.scarsz.configuralize", "ac.darkness.darkness.shaded.configuralize")
        relocate("com.github.puregero", "ac.darkness.darkness.shaded.com.github.puregero")
        relocate("com.google.code.gson", "ac.darkness.darkness.shaded.gson")
        relocate("alexh", "ac.darkness.darkness.shaded.maps")
        relocate("it.unimi.dsi.fastutil", "ac.darkness.darkness.shaded.fastutil")
        relocate("okhttp3", "ac.darkness.darkness.shaded.okhttp3")
        relocate("okio", "ac.darkness.darkness.shaded.okio")
        relocate("org.yaml.snakeyaml", "ac.darkness.darkness.shaded.snakeyaml")
        relocate("org.json", "ac.darkness.darkness.shaded.json")
        relocate("org.intellij", "ac.darkness.darkness.shaded.intellij")
        relocate("org.jetbrains", "ac.darkness.darkness.shaded.jetbrains")
        relocate("org.incendo", "ac.darkness.darkness.shaded.incendo")
        relocate("io.leangen.geantyref", "ac.darkness.darkness.shaded.geantyref") // Required by cloud
        relocate("com.zaxxer", "ac.darkness.darkness.shaded.zaxxer") // Database history
    }
    mergeServiceFiles()
}

tasks.named("assemble") {
    dependsOn(tasks.named("shadowJar"))
}
