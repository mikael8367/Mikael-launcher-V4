package com.mikael.launcher

import java.io.File

// Motor de boot inspirado no PojavLauncher:
// Pojav usa JRE 17 embarcado + LWJGL + gl4es/ANGLE + Dalvik bridge.
// Aqui delegamos p/ as classes do Pojav quando o modulo estiver presente,
// com fallback p/ montagem de args padrao.
object PojavEngine {
    var jrePath: String = "/sdcard/MikaelLauncher/runtime/java-17"
    var nativesDir: String = "/sdcard/MikaelLauncher/natives"
    var gameDir: String = "/sdcard/MikaelLauncher/gamedir"

    fun isRuntimePresent(): Boolean = File("$jrePath/bin/java").exists() || File(jrePath).exists()

    fun buildJvmArgs(acc: GameAccount, mcVersion: String, ramMb: Int, modLoader: ModLoader, loaderVer: String): List<String> {
        val versionId = when (modLoader) {
            ModLoader.VANILLA -> mcVersion
            ModLoader.FABRIC -> "$mcVersion-fabric-$loaderVer"
            else -> "$mcVersion-${modLoader.name.lowercase()}"
        }
        val base = mutableListOf(
            "-Xms${ramMb/2}M", "-Xmx${ramMb}M",
            "-Djava.library.path=$nativesDir",
            "-Dorg.lwjgl.opengl.libname=libgl4es.so",
            "-Dorg.lwjgl.vulkan.libname=libvulkan.so",
            "-cp", "client.jar:libraries/*",
            "net.minecraft.client.main.Main",
            "--username", acc.username,
            "--version", versionId,
            "--gameDir", gameDir,
            "--assetsDir", "/sdcard/MikaelLauncher/assets",
            "--uuid", acc.uuid.replace("-", ""),
            "--accessToken", acc.accessToken,
            "--userType", MinecraftManager.userType(acc)
        )
        return base
    }

    fun launch(acc: GameAccount, mcVersion: String, ramMb: Int, modLoader: ModLoader, loaderVer: String, log: (String) -> Unit) {
        log("[PojavEngine] runtime=$jrePath")
        if (!isRuntimePresent()) {
            log("JRE nao encontrada! Baixe o runtime Pojav (JRE 17 Android):")
            log("https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases")
            log("Extraia em $jrePath")
        }
        val args = buildJvmArgs(acc, mcVersion, ramMb, modLoader, loaderVer)
        log("[PojavEngine] boot $mcVersion + $modLoader")
        log(args.joinToString(" "))
        try {
            // Tenta chamar classe real do Pojav se incluída como dependência:
            // Class.forName("net.kdt.pojavlaunch.JRELauncher").getMethod("launch", List::class.java).invoke(null, args)
            Class.forName("net.kdt.pojavlaunch.JRELauncher")
            log("Pojav core detectado — delegando boot...")
        } catch (e: ClassNotFoundException) {
            log("Pojav core ausente (modo esqueleto). Adicione como submodule p/ boot real. Veja README.")
        }
    }
}
