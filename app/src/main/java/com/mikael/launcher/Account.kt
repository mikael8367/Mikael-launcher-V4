package com.mikael.launcher

enum class AccountType { OFFLINE, MICROSOFT, ELYBY }

data class GameAccount(
    val type: AccountType,
    val username: String,
    val uuid: String = offlineUuid(username),
    val accessToken: String = "0",
    val refreshToken: String = "",
    val elyByToken: String = ""
) {
    companion object {
        fun offlineUuid(name: String): String {
            val h = "OfflinePlayer:$name".hashCode()
            return "00000000-0000-3000-8000-%012x".format(h.toLong() and 0xffffffffffL)
        }
    }
    override fun toString(): String = when (type) {
        AccountType.OFFLINE -> "[Offline] $username"
        AccountType.MICROSOFT -> "[Microsoft] $username"
        AccountType.ELYBY -> "[ely.by] $username"
    }
}
