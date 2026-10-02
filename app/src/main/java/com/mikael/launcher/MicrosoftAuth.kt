package com.mikael.launcher

import android.content.Intent
import android.net.Uri

object MicrosoftAuth {
    // Troque pelo seu Azure App Client ID para login real
    const val CLIENT_ID = "SEU_CLIENT_ID_AZURE"
    const val REDIRECT = "mikael-launcher://auth"

    fun loginUrl(): String {
        return "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize" +
            "?client_id=$CLIENT_ID" +
            "&response_type=code" +
            "&redirect_uri=${Uri.encode(REDIRECT)}" +
            "&scope=${Uri.encode("XboxLive.signin offline_access")}" +
            "&prompt=select_account"
    }

    fun openLogin(activity: MainActivity) {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl()))
        activity.startActivity(i)
    }

    // Fluxo completo: code -> token MS -> XBL -> XSTS -> Minecraft token -> perfil
    // Implementar no backend ou com MSAL. Por enquanto o app aceita colar nick+token manual.
    fun fromManual(username: String, mcToken: String, uuid: String): GameAccount {
        return GameAccount(
            type = AccountType.MICROSOFT,
            username = username,
            uuid = uuid.ifBlank { Account.offlineUuid(username) },
            accessToken = mcToken
        )
    }

    private object Account {
        fun offlineUuid(name: String): String {
            val h = "OfflinePlayer:$name".hashCode()
            return "00000000-0000-3000-8000-%012x".format(h.toLong() and 0xffffffffffL)
        }
    }
}
