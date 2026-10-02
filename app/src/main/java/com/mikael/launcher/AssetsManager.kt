package com.mikael.launcher

import org.json.JSONObject
import java.io.File

object AssetsManager {
    fun download(versionJson: JSONObject, base: File, log: (String) -> Unit) {
        val idx = versionJson.getJSONObject("assetIndex")
        val url = idx.getString("url")
        val id = idx.getString("id")
        val indexesDir = File(base, "assets/indexes")
        val objectsDir = File(base, "assets/objects")
        indexesDir.mkdirs(); objectsDir.mkdirs()
        val idxFile = File(indexesDir, "$id.json")
        if (!idxFile.exists()) { log("Baixando assets index $id..."); DownloadUtils.download(url, idxFile) }
        val json = JSONObject(idxFile.readText())
        val objs = json.getJSONObject("objects")
        log("${objs.length()} assets...")
        var n = 0
        objs.keys().forEach { name ->
            val h = objs.getJSONObject(name).getString("hash")
            val sub = h.substring(0, 2)
            val dest = File(objectsDir, "$sub/$h")
            if (!dest.exists()) {
                try { DownloadUtils.download("https://resources.download.minecraft.net/$sub/$h", dest) } catch (e: Exception) { log("falha asset $name") }
            }
            n++
            if (n % 200 == 0) log("assets $n/${objs.length()}")
        }
        log("assets OK")
    }
}
