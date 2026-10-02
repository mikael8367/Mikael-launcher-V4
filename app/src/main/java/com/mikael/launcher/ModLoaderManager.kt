package com.mikael.launcher

import java.io.File

enum class ModLoader { VANILLA, FABRIC, FORGE, NEOFORGE, QUILT }

object ModLoaderManager {

    // ---------- Fabric ----------
    fun fabricVersions(mc: String): List<String> {
        return try {
            val txt = DownloadUtils.getText("https://meta.fabricmc.net/v2/versions/loader/$mc")
            val arr = org.json.JSONArray(txt)
            (0 until arr.length()).map { i -> arr.getJSONObject(i).getJSONObject("loader").getString("version") }.take(5)
        } catch (e: Exception) { listOf("0.16.9") }
    }

    fun installFabric(mc: String, loader: String, baseDir: File, log: (String) -> Unit) {
        val url = "https://meta.fabricmc.net/v2/versions/loader/$mc/$loader/profile/json"
        log("Baixando perfil Fabric $mc+$loader...")
        val txt = DownloadUtils.getText(url)
        File(baseDir, "versions/$mc-fabric-$loader.json").apply { parentFile?.mkdirs(); writeText(txt) }
        log("Fabric instalado: $mc-fabric-$loader")
    }

    // ---------- Forge (ordem: versao MC -> versao Forge) ----------
    fun forgeVersions(mc: String): List<String> {
        return try {
            val xml = DownloadUtils.getText("https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml")
            Regex("<version>([^<]+)</version>").findAll(xml)
                .map { it.groupValues[1] }
                .filter { it.startsWith("$mc-") }
                .map { it.removePrefix("$mc-") }
                .toList().takeLast(10).reversed()
        } catch (e: Exception) { emptyList() }
    }

    fun forgeInstallerUrl(mc: String, forge: String): String {
        return "https://maven.minecraftforge.net/net/minecraftforge/forge/$mc-$forge/forge-$mc-$forge-installer.jar"
    }

    fun installForge(mc: String, forge: String, baseDir: File, log: (String) -> Unit) {
        val dest = File(baseDir, "installers/forge-$mc-$forge-installer.jar")
        if (!dest.exists()) {
            log("Baixando Forge $forge p/ $mc...")
            DownloadUtils.download(forgeInstallerUrl(mc, forge), dest)
        } else log("Installer Forge ja existe")
        File(baseDir, "gamedir/mods").mkdirs()
        File(baseDir, "versions/$mc-forge-$forge.txt").apply {
            parentFile?.mkdirs()
            writeText("Forge $forge p/ $mc.\nInstaller: installers/${dest.name}\nMods vao em gamedir/mods/")
        }
        log("Forge pronto: $mc + $forge")
    }

    // ---------- OptiFine (ordem: versao Forge -> versao OptiFine) ----------
    // Tabela curada das edicoes mais usadas por versao do MC
    private val optifineTable = mapOf(
        "1.21" to listOf("HD_U_J1", "HD_U_J2"),
        "1.20.4" to listOf("HD_U_I7", "HD_U_I6"),
        "1.20.1" to listOf("HD_U_I6", "HD_U_I5", "HD_U_I4", "HD_U_I1"),
        "1.19.4" to listOf("HD_U_I2", "HD_U_I1", "HD_U_H9"),
        "1.19.2" to listOf("HD_U_H9", "HD_U_H8", "HD_U_H6"),
        "1.18.2" to listOf("HD_U_H9", "HD_U_H8", "HD_U_H7", "HD_U_H6"),
        "1.16.5" to listOf("HD_U_G9", "HD_U_G8", "HD_U_G7", "HD_U_G5"),
        "1.12.2" to listOf("HD_U_G5", "HD_U_G4", "HD_U_F5"),
        "1.8.9" to listOf("HD_U_M5", "HD_U_M4", "HD_U_L5"),
        "1.7.10" to listOf("HD_U_E7", "HD_U_D8")
    )

    fun optifineEditions(mc: String): List<String> {
        return optifineTable[mc] ?: listOf("HD_U_I6", "HD_U_H9", "HD_U_G9")
    }

    fun optifineFile(mc: String, edition: String) = "OptiFine_${mc}_${edition}.jar"

    fun optifinePageUrl() = "https://optifine.net/downloads"

    // OptiFine exige clique no site oficial: abre a pagina e importa o jar da pasta Download
    fun importOptifineFromDownload(mc: String, edition: String, baseDir: File, log: (String) -> Unit): Boolean {
        val want = optifineFile(mc, edition)
        val candidates = listOf(
            File("/sdcard/Download"),
            File("/storage/emulated/0/Download")
        )
        for (dir in candidates) {
            val exact = File(dir, want)
            if (exact.exists()) return copyMod(exact, baseDir, log)
            // aceita qualquer OptiFine do mesmo MC que esteja na pasta
            dir.listFiles { f -> f.name.startsWith("OptiFine_${mc}_") && f.name.endsWith(".jar") }
                ?.firstOrNull()?.let { return copyMod(it, baseDir, log) }
        }
        log("OptiFine nao achado em Download. Baixe $want em optifine.net e toque em Baixar de novo.")
        return false
    }

    private fun copyMod(jar: File, baseDir: File, log: (String) -> Unit): Boolean {
        val mods = File(baseDir, "gamedir/mods")
        mods.mkdirs()
        jar.copyTo(File(mods, jar.name), overwrite = true)
        log("OptiFine instalado em mods: ${jar.name}")
        return true
    }

    // ---------- outros ----------
    fun installNeoForge(mc: String, baseDir: File, log: (String) -> Unit) {
        log("NeoForge p/ $mc: via https://maven.neoforged.net/releases/net/neoforged/neoforge/")
        File(baseDir, "versions/$mc-neoforge.txt").apply {
            parentFile?.mkdirs(); writeText("NeoForge $mc -> https://neoforged.com/")
        }
        log("NeoForge registrado.")
    }

    fun installQuilt(mc: String, baseDir: File, log: (String) -> Unit) {
        val txt = try { DownloadUtils.getText("https://meta.quiltmc.org/v3/versions/loader/$mc") } catch (e: Exception) { "" }
        File(baseDir, "versions/$mc-quilt.json").apply { parentFile?.mkdirs(); writeText(txt.ifBlank { "{}" }) }
        log("Quilt registrado p/ $mc")
    }
}
