package com.mikael.launcher

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var txtLog: TextView
    private lateinit var labelRam: TextView
    private lateinit var spinnerAccounts: Spinner
    private lateinit var spinnerAccType: Spinner
    private lateinit var spinnerVersion: Spinner
    private lateinit var spinnerModloader: Spinner
    private lateinit var accounts: MutableList<GameAccount>
    private lateinit var accManager: AccountManager

    private var vanilla: List<McVersion> = listOf(
        McVersion("1.21", "release", ""),
        McVersion("1.20.1", "release", ""),
        McVersion("1.19.4", "release", ""),
        McVersion("1.16.5", "release", ""),
        McVersion("1.8.9", "release", "")
    )
    private val loaderNames = listOf("Vanilla", "Fabric", "Forge", "NeoForge", "Quilt")
    private val types = listOf("Offline", "Microsoft", "ely.by")

    private fun baseDir(): File = File("/sdcard/MikaelLauncher")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        accManager = AccountManager(this)
        accounts = accManager.getAll()
        if (accounts.isEmpty()) accounts.add(GameAccount(AccountType.OFFLINE, "Mikael"))

        val inputUser = findViewById<EditText>(R.id.inputUsername)
        val inputEmail = findViewById<EditText>(R.id.inputEmail)
        val inputPass = findViewById<EditText>(R.id.inputPass)
        spinnerVersion = findViewById(R.id.spinnerVersion)
        spinnerModloader = findViewById(R.id.spinnerModloader)
        val seekRam = findViewById<SeekBar>(R.id.seekRam)
        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnDownload = findViewById<Button>(R.id.btnDownload)
        val btnAll = findViewById<Button>(R.id.btnDownloadAll)
        val btnRefresh = findViewById<Button>(R.id.btnRefreshVersions)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val btnRemove = findViewById<Button>(R.id.btnRemove)
        val btnMs = findViewById<Button>(R.id.btnMicrosoft)
        txtLog = findViewById(R.id.txtLog)
        labelRam = findViewById(R.id.labelRam)
        spinnerAccounts = findViewById(R.id.spinnerAccounts)
        spinnerAccType = findViewById(R.id.spinnerAccType)

        spinnerAccType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)
        spinnerModloader.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, loaderNames)
        refreshVersionsUI(); refreshAccounts()

        spinnerAccType.onItemSelectedListener = sel { pos ->
            inputUser.visibility = if (pos == 0) android.view.View.VISIBLE else android.view.View.GONE
            inputEmail.visibility = if (pos == 0) android.view.View.GONE else android.view.View.VISIBLE
            inputPass.visibility = if (pos == 0) android.view.View.GONE else android.view.View.VISIBLE
            btnMs.visibility = if (pos == 1) android.view.View.VISIBLE else android.view.View.GONE
        }

        seekRam.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, v: Int, f: Boolean) { labelRam.text = "RAM (MB): $v" }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        btnAdd.setOnClickListener {
            when (spinnerAccType.selectedItemPosition) {
                0 -> { val n = inputUser.text.toString().ifBlank { "Mikael" }; accManager.add(GameAccount(AccountType.OFFLINE, n)); refreshAccounts(); log("Conta offline: $n") }
                1 -> { val t = inputPass.text.toString(); if (t.isBlank()) log("Microsoft: cole o Token MC ou use o botao Microsoft.") else { val n = inputEmail.text.toString().substringBefore("@"); accManager.add(MicrosoftAuth.fromManual(n, t, "")); refreshAccounts(); log("Microsoft manual: $n") } }
                2 -> { val l = inputEmail.text.toString(); val p = inputPass.text.toString(); if (l.isBlank() || p.isBlank()) { log("ely.by: preencha login/senha") } else { log("Conectando ely.by..."); Thread { try { val a = ElyByAuth.login(l, p); runOnUiThread { accManager.add(a); refreshAccounts(); log("ely.by: ${a.username}") } } catch (e: Exception) { runOnUiThread { log("Falha ely.by: ${e.message}") } } }.start() } }
            }
        }
        btnRemove.setOnClickListener {
            if (accounts.isNotEmpty()) { val a = accounts[spinnerAccounts.selectedItemPosition]; accManager.remove(a); refreshAccounts(); log("Removida: $a") }
        }
        btnMs.setOnClickListener {
            if (MicrosoftAuth.CLIENT_ID == "SEU_CLIENT_ID_AZURE") log("Configure CLIENT_ID Azure em MicrosoftAuth.kt")
            MicrosoftAuth.openLogin(this)
        }

        btnRefresh.setOnClickListener {
            log("Buscando Vanilla (piston-meta)...")
            Thread {
                try { vanilla = VersionManager.fetchVanilla(); runOnUiThread { refreshVersionsUI(); log("${vanilla.size} versoes Vanilla") } }
                catch (e: Exception) { runOnUiThread { log("Falha manifest: ${e.message} (usando lista local)") } }
            }.start()
        }

        btnDownload.setOnClickListener {
            val mc = vanilla[spinnerVersion.selectedItemPosition].id
            val loader = ModLoader.values()[spinnerModloader.selectedItemPosition]
            log("Download $mc + $loader ...")
            Thread {
                try {
                    val v = vanilla[spinnerVersion.selectedItemPosition]
                    if (v.url.isNotBlank()) VersionManager.downloadClient(v.id, v.url, baseDir()) { log(it) }
                    else log("Sem URL manifest (offline) — pulando client.jar, indo p/ modloader")
                    when (loader) {
                        ModLoader.VANILLA -> runOnUiThread { log("Vanilla pronta: $mc") }
                        ModLoader.FABRIC -> { val lv = ModLoaderManager.fabricVersions(mc).first(); ModLoaderManager.installFabric(mc, lv, baseDir()) { s -> runOnUiThread { log(s) } } }
                        ModLoader.FORGE -> ModLoaderManager.installForge(mc, baseDir()) { s -> runOnUiThread { log(s) } }
                        ModLoader.NEOFORGE -> ModLoaderManager.installNeoForge(mc, baseDir()) { s -> runOnUiThread { log(s) } }
                        ModLoader.QUILT -> ModLoaderManager.installQuilt(mc, baseDir()) { s -> runOnUiThread { log(s) } }
                    }
                    runOnUiThread { log("Download concluido: $mc + $loader") }
                } catch (e: Exception) { runOnUiThread { log("Erro download: ${e.message}") } }
            }.start()
        }

        btnAll.setOnClickListener {
            val mc = vanilla[spinnerVersion.selectedItemPosition]
            val loader = ModLoader.values()[spinnerModloader.selectedItemPosition]
            log("Iniciando download total no celular...")
            Thread {
                try { FullDownloadManager.downloadAll(mc.id, mc.url, loader, baseDir()) { m -> runOnUiThread { log(m) } } }
                catch (e: Exception) { runOnUiThread { log("Erro total: ${e.message}") } }
            }.start()
        }

        btnPlay.setOnClickListener {
            if (accounts.isEmpty()) { log("Crie uma conta"); return@setOnClickListener }
            val acc = accounts[spinnerAccounts.selectedItemPosition]
            val mc = vanilla[spinnerVersion.selectedItemPosition].id
            val loader = ModLoader.values()[spinnerModloader.selectedItemPosition]
            val loaderVer = if (loader == ModLoader.FABRIC) "0.16.9" else ""
            PojavEngine.launch(acc, mc, seekRam.progress, loader, loaderVer) { runOnUiThread { log(it) } }
        }
    }

    private fun refreshAccounts() {
        accounts = accManager.getAll()
        spinnerAccounts.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, accounts)
    }
    private fun refreshVersionsUI() {
        spinnerVersion.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, vanilla.map { it.id })
    }
    private fun log(m: String) { txtLog.append("\n$m") }
    private fun sel(cb: (Int) -> Unit) = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) = cb(pos)
        override fun onNothingSelected(p: AdapterView<*>?) {}
    }
}
