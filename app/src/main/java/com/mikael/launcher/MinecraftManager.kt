package com.mikael.launcher

object MinecraftManager {
    val versions = listOf("1.21", "1.20.1", "1.19.4", "1.16.5", "1.8.9")

    fun userType(acc: GameAccount): String = when (acc.type) {
        AccountType.OFFLINE -> "legacy"
        AccountType.MICROSOFT -> "msa"
        AccountType.ELYBY -> "mojang"
    }

    fun buildLaunchArgs(acc: GameAccount, version: String, ramMb: Int): List<String> {
        return listOf(
            "-Xms${ramMb / 2}M",
            "-Xmx${ramMb}M",
            "-Djava.library.path=natives",
            "-cp", "client.jar:libraries/*",
            "net.minecraft.client.main.Main",
            "--username", acc.username,
            "--version", version,
            "--gameDir", "/sdcard/MikaelLauncher/gamedir",
            "--assetsDir", "/sdcard/MikaelLauncher/assets",
            "--uuid", acc.uuid.replace("-", ""),
            "--accessToken", acc.accessToken,
            "--userType", userType(acc)
        )
    }
}
