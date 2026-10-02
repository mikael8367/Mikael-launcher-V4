package com.mikael.launcher

import org.json.JSONObject
import java.io.File

data class McVersion(val id: String, val type: String, val url: String)

object VersionManager {
    const val MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

    fun fetchVanilla(): List<McVersion> {
        val json = DownloadUtils.getJson(MANIFEST)
        val arr = json.getJSONArray("versions")
        val out = mutableListOf<McVersion>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("type") == "release") {
                out.add(McVersion(o.getString("id"), o.getString("type"), o.getString("url")))
            }
        }
        return out.take(30)
    }

    fun downloadClient(versionId: String, versionUrl: String, baseDir: File, log: (String) -> Unit) {
        log("Baixando manifest $versionId...")
        val vJson = DownloadUtils.getJson(versionUrl)
        val client = vJson.getJSONObject("downloads").getJSONObject("client")
        val clientUrl = client.getString("url")
        val dest = File(baseDir, "versions/$versionId/client.jar")
        if (!dest.exists()) {
            log("Baixando client.jar...")
            DownloadUtils.download(clientUrl, dest) { done, total ->
                if (total > 0 && done % (1024*1024) < 65536) log("client.jar ${(done*100/total)}%")
            }
            log("client.jar OK")
        } else log("client.jar ja existe")

        // salva version.json p/ PojavEngine montar classpath
        File(baseDir, "versions/$versionId/version.json").apply {
            parentFile?.mkdirs(); writeText(vJson.toString())
        }
        // libraries (simplificado: loga total, download real feito no launch)
        val libs = vJson.getJSONArray("libraries")
        log("${libs.length()} libs declaradas (download sob demanda no boot)")
    }
}
