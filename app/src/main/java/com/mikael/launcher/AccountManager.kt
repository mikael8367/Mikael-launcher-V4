package com.mikael.launcher

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class AccountManager(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("mikael_accounts", Context.MODE_PRIVATE)

    fun getAll(): MutableList<GameAccount> {
        val out = mutableListOf<GameAccount>()
        val arr = try { JSONArray(prefs.getString("accounts", "[]")) } catch (e: Exception) { JSONArray() }
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(GameAccount(
                type = AccountType.valueOf(o.optString("type", "OFFLINE")),
                username = o.optString("username", "Mikael"),
                uuid = o.optString("uuid"),
                accessToken = o.optString("accessToken", "0"),
                refreshToken = o.optString("refreshToken", ""),
                elyByToken = o.optString("elyByToken", "")
            ))
        }
        return out
    }

    fun saveAll(list: List<GameAccount>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject()
                .put("type", it.type.name)
                .put("username", it.username)
                .put("uuid", it.uuid)
                .put("accessToken", it.accessToken)
                .put("refreshToken", it.refreshToken)
                .put("elyByToken", it.elyByToken))
        }
        prefs.edit().putString("accounts", arr.toString()).apply()
    }

    fun add(acc: GameAccount) {
        val all = getAll()
        all.removeAll { it.username == acc.username && it.type == acc.type }
        all.add(acc)
        saveAll(all)
    }

    fun remove(acc: GameAccount) {
        val all = getAll().filterNot { it.username == acc.username && it.type == acc.type }
        saveAll(all)
    }
}
