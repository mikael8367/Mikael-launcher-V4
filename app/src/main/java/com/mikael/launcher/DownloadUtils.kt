package com.mikael.launcher

import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object DownloadUtils {
    fun getJson(url: String): JSONObject {
        val con = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000; readTimeout = 15000
            setRequestProperty("User-Agent", "Mikael-Launcher/1.0")
        }
        val txt = con.inputStream.bufferedReader().readText()
        return JSONObject(txt)
    }

    fun getText(url: String): String {
        val con = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000; readTimeout = 15000
            setRequestProperty("User-Agent", "Mikael-Launcher/1.0")
        }
        return con.inputStream.bufferedReader().readText()
    }

    fun download(url: String, dest: File, onProgress: (Long, Long) -> Unit = {_,_ ->}) {
        dest.parentFile?.mkdirs()
        val con = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000; readTimeout = 30000
            setRequestProperty("User-Agent", "Mikael-Launcher/1.0")
        }
        val total = con.contentLengthLong
        con.inputStream.use { inp ->
            FileOutputStream(dest).use { out ->
                val buf = ByteArray(64 * 1024)
                var done = 0L
                while (true) {
                    val r = inp.read(buf)
                    if (r <= 0) break
                    out.write(buf, 0, r)
                    done += r
                    onProgress(done, total)
                }
            }
        }
    }
}
