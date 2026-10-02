# Mikael Launcher

Launcher base de Minecraft Java para Android (modo offline).

## Abrir e gerar APK
1. Abra o Android Studio > Open > pasta `mikael-launcher`
2. Deixe ele sincronizar o Gradle (precisa de JDK 17)
3. Run no celular ou Build > Build APK(s)

## Estrutura
- `MainActivity.kt` - tela inicial, nick + versao + RAM + botao JOGAR
- `MinecraftManager.kt` - lista de versoes e montagem dos args JVM
- `activity_main.xml` - layout simples

## Proximo passo p/ rodar Java de verdade
Este projeto e o esqueleto. Para boot real do Minecraft Java voce precisa integrar:
- Java Runtime embarcado (ex: JRE 17 for Android do PojavLauncherTeam)
- Natives LWJGL + gl4es / ANGLE
- Download do client.jar via Mojang piston-meta

Recomendo forkar o PojavLauncher e trocar o pacote/tema para "Mikael".

## Contas (novo)
- Offline: so digite o nick e Criar
- ely.by: selecione ely.by, digite login/senha, Criar (autentica em authserver.ely.by)
- Microsoft: exige CLIENT_ID Azure em `MicrosoftAuth.kt`. Sem ele, use modo manual (cole Token MC).

## Download Vanilla + Modloaders (novo)
- Botao ↻ busca `piston-meta.mojang.com` (30 releases)
- `Baixar versão` baixa `client.jar` + `version.json` em `/sdcard/MikaelLauncher/versions/<id>/`
- Modloaders:
  - Fabric: via `meta.fabricmc.net` (perfil json automatico)
  - Quilt: via `meta.quiltmc.org`
  - Forge/NeoForge: registra manual (installer oficial em `/sdcard/MikaelLauncher/installers/`)

## Motor PojavLauncher (novo)
`PojavEngine.kt` monta os args JVM estilo Pojav (JRE 17 + gl4es + natives).
Para boot REAL:
1. `git submodule add https://github.com/PojavLauncherTeam/PojavLauncher pojav`
2. Copie o JRE de `https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases` para `/sdcard/MikaelLauncher/runtime/java-17`
3. Adicione em `app/build.gradle`:
```
implementation project(':pojav:app_pojavlauncher')
```
4. Descomente a chamada `JRELauncher.launch()` em `PojavEngine.kt`
Sem o core, o botao JOGAR so loga os args (modo esqueleto).

## Baixar tudo no Android (novo)
- Botao `Baixar TUDO no celular` baixa em `/sdcard/MikaelLauncher/`:
  - `versions/<mc>/client.jar` + `version.json`
  - `libraries/` (via LibrariesManager, pulando natives PC)
  - `assets/indexes` + `assets/objects`
  - `runtime/java-17/` (instrucao JRE Pojav) + `natives/` + `gamedir/`
- Tudo roda 100% no celular, sem PC.

## Gerar APK direto pelo celular (GitHub)
1. Suba esta pasta p/ um repo GitHub
2. Aba Actions > Build Mikael APK > Run
3. Baixe o artefato `mikael-launcher-apk` no seu Android e instale
