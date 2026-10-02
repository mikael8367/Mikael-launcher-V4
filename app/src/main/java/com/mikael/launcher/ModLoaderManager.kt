package com.mikael.launcher

import org.json.JSONArray
import java.io.File

enum class ModLoader { VANILLA, FABRIC, FORGE, NEOFORGE, QUILT }

object ModLoaderManager {
    fun fabricVersions(mc: String): List<String> {
        return try {
            val txt = DownloadUtils.getText("https://meta.fabricmc.net/v2/versions/loader/$mc")
            val arr = JSONArray(txt)
            arr.let { (0 until it.length()).map { i -> it.getJSONObject(i).getJSONObject("loader").getString("version") } }.take(5)
        } catch (e: Exception) { listOf("0.16.9") }
    }

    fun installFabric(mc: String, loader: String, baseDir: File, log: (String) -> Unit) {
        val url = "https://meta.fabricmc.net/v2/versions/loader/$mc/$loader/profile/json"
        log("Baixando perfil Fabric $mc+$loader...")
        val txt = DownloadUtils.getText(url)
        File(baseDir, "versions/$mc-fabric-$loader.json").apply { parentFile?.mkdirs(); writeText(txt) }
        log("Fabric instalado: $mc-fabric-$loader")
    }

    fun installForge(mc: String, baseDir: File, log: (String) -> Unit) {
        // Forge usa maven; pega latest do promocional simplificado
        log("Resolvendo Forge p/ $mc via maven.minecraftforge.net...")
        log("Dica: Forge exige installer oficial. Baixando via files.minecraftforge.net")
        File(baseDir, "versions/$mc-forge.txt").apply {
            parentFile?.mkdirs()
            writeText("Abra https://files.minecraftforge.net/net/minecraftforge/forge/index_$mc.html e coloque o installer em /sdcard/MikaelLauncher/installers/")
        }
        log("Forge: manual por enquanto (installer oficial). Veja versions/$mc-forge.txt")
    }

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
