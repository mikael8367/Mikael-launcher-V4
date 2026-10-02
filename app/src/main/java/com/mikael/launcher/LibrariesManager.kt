package com.mikael.launcher

import org.json.JSONObject
import java.io.File

object LibrariesManager {
    fun downloadAll(versionJson: JSONObject, libsDir: File, log: (String) -> Unit) {
        val libs = versionJson.getJSONArray("libraries")
        var ok = 0; var skip = 0
        for (i in 0 until libs.length()) {
            val lib = libs.getJSONObject(i)
            if (!allow(lib)) { skip++; continue }
            val artifact = lib.optJSONObject("downloads")?.optJSONObject("artifact") ?: continue
            val path = artifact.getString("path")
            val url = artifact.getString("url")
            val dest = File(libsDir, path)
            if (dest.exists()) { ok++; continue }
            try {
                DownloadUtils.download(url, dest)
                ok++
                if (ok % 10 == 0) log("libs $ok/${libs.length()}")
            } catch (e: Exception) { log("falha lib $path: ${e.message}") }
        }
        log("libs OK: $ok, puladas: $skip")
    }

    private fun allow(lib: JSONObject): Boolean {
        if (!lib.has("rules")) return true
        val rules = lib.getJSONArray("rules")
        var allowed = false
        for (i in 0 until rules.length()) {
            val r = rules.getJSONObject(i)
            val action = r.getString("action")
            val os = r.optJSONObject("os")?.optString("name", "")
            if (os.isNullOrBlank()) { if (action == "allow") allowed = true }
            // no Android pulamos natives windows/osx
        }
        return allowed
    }
}
