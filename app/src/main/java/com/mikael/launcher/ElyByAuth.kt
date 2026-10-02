package com.mikael.launcher

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object ElyByAuth {
    fun login(login: String, password: String): GameAccount {
        val url = URL("https://authserver.ely.by/auth/authenticate")
        val body = JSONObject()
            .put("username", login)
            .put("password", password)
            .put("clientToken", UUID.randomUUID().toString().replace("-", ""))
            .put("requestUser", true)
            .toString()

        val con = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 15000
            readTimeout = 15000
        }
        con.outputStream.use { it.write(body.toByteArray()) }
        val code = con.responseCode
        val resp = (if (code == 200) con.inputStream else con.errorStream).bufferedReader().readText()
        if (code != 200) throw Exception("ely.by erro $code: $resp")

        val json = JSONObject(resp)
        val profile = json.getJSONObject("selectedProfile")
        return GameAccount(
            type = AccountType.ELYBY,
            username = profile.getString("name"),
            uuid = profile.getString("id"),
            accessToken = json.getString("accessToken"),
            elyByToken = json.optString("clientToken", "")
        )
    }
}
