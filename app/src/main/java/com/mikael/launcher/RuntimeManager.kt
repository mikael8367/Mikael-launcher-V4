package com.mikael.launcher

import java.io.File

object RuntimeManager {
    // JRE 17 Android do PojavLauncherTeam (multiarch)
    const val JRE_URL_AARCH64 = "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/20230805-…/jre17-android-aarch64.tar.xz"

    fun runtimeDir(base: File) = File(base, "runtime/java-17")
    fun isInstalled(base: File): Boolean {
        val d = runtimeDir(base)
        return File(d, "bin/java").exists() || File(d, "lib/server/libjvm.so").exists()
    }
    fun install(base: File, log: (String) -> Unit) {
        val dir = runtimeDir(base)
        if (isInstalled(base)) { log("JRE 17 ja instalado"); return }
        dir.mkdirs()
        log("Baixe o JRE 17 Pojav e extraia em: ${dir.absolutePath}")
        log("Link: github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases")
        File(dir, "LEIA-ME.txt").writeText("Extraia o jre17-android-aarch64.tar.xz aqui.\nEx: $dir/bin/java deve existir.")
    }
}
