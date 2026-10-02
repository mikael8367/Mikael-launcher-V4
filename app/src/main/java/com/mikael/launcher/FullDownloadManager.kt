package com.mikael.launcher

import org.json.JSONObject
import java.io.File

object FullDownloadManager {
    fun downloadAll(mc: String, versionUrl: String, modLoader: ModLoader, base: File, log: (String) -> Unit) {
        base.mkdirs()
        File(base, "gamedir").mkdirs()
        File(base, "installers").mkdirs()
        log("== Download total $mc + $modLoader ==")
        // 1. version json + client
        var vJson: JSONObject? = null
        if (versionUrl.isNotBlank()) {
            VersionManager.downloadClient(mc, versionUrl, base, log)
            vJson = try { JSONObject(File(base, "versions/$mc/version.json").readText()) } catch (e: Exception) { null }
        }
        // 2. modloader perfil
        when (modLoader) {
            ModLoader.FABRIC -> { val lv = ModLoaderManager.fabricVersions(mc).first(); ModLoaderManager.installFabric(mc, lv, base, log) }
            ModLoader.FORGE -> { val fv = ModLoaderManager.forgeVersions(mc).firstOrNull() ?: "latest"; ModLoaderManager.installForge(mc, fv, base, log) }
            ModLoader.NEOFORGE -> ModLoaderManager.installNeoForge(mc, base, log)
            ModLoader.QUILT -> ModLoaderManager.installQuilt(mc, base, log)
            else -> {}
        }
        // 3. libs + assets
        if (vJson != null) {
            log("Baixando libraries...");
            LibrariesManager.downloadAll(vJson, File(base, "libraries"), log)
            log("Baixando assets...");
            AssetsManager.download(vJson, base, log)
        } else log("Sem version.json — libs/assets pulados (offline)")
        // 4. runtime + natives
        RuntimeManager.install(base, log)
        File(base, "natives/LEIA-ME.txt").apply { parentFile?.mkdirs(); if (!exists()) writeText("Natives LWJGL/gl4es do Pojav vao aqui (libgl4es.so, etc).") }
        log("== Tudo pronto em ${base.absolutePath} ==")
    }
}
