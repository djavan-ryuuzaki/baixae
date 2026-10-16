# Publicação no GitHub Releases

Cada versão é publicada como três APKs assinados, um por ABI (arm64-v8a, armeabi-v7a, x86_64), anexados a uma release do GitHub. Ver [ADR-0001](adr/0001-distribuicao-fora-da-play-store.md).

## Uma vez só: chave de assinatura

Todas as versões precisam ser assinadas com **a mesma chave**; sem ela, quem já instalou não consegue atualizar. Guarde o arquivo `.jks` e as senhas fora do repositório (e com backup).

```bash
keytool -genkeypair -v -keystore socialvideodownloader.jks -alias socialvideodownloader -keyalg RSA -keysize 4096 -validity 36500
```

Crie `keystore.properties` na raiz do projeto (já está no `.gitignore`):

```properties
storeFile=C:/caminho/para/socialvideodownloader.jks
storePassword=...
keyAlias=socialvideodownloader
keyPassword=...
```

Sem esse arquivo, `assembleRelease` gera APKs **não assinados**, que não instalam.

Preencha também, em `gradle.properties`, o repositório usado pelo aviso de nova versão:

```properties
repositorioGithub=dono/SocialVideoDownloader
```

## A cada versão

1. Em `app/build.gradle.kts`, aumente `versionCode` (de 1 em 1) e ajuste `versionName` (ex.: `0.2.0`). Cada APK recebe `versionCode * 10 + n` (armeabi-v7a = 1, arm64-v8a = 2, x86_64 = 3).
2. Rode os testes e gere os APKs:

   ```bash
   ./gradlew :core:test :app:testDebugUnitTest :app:assembleRelease
   ```

3. Confira a assinatura de um dos APKs:

   ```bash
   apksigner verify --print-certs app/build/outputs/apk/release/app-arm64-v8a-release.apk
   ```

4. Instale o APK numa versão anterior já instalada (para confirmar que atualiza sem perder dados) e faça um Download de teste de cada Plataforma.
5. Renomeie os APKs para `SocialVideoDownloader-<versão>-<abi>.apk`, crie a tag e a release:

   ```bash
   git tag v0.2.0
   git push origin v0.2.0
   gh release create v0.2.0 --title "v0.2.0" --notes-file notas.md SocialVideoDownloader-0.2.0-arm64-v8a.apk SocialVideoDownloader-0.2.0-armeabi-v7a.apk SocialVideoDownloader-0.2.0-x86_64.apk
   ```

   O nome da tag precisa ser a `versionName` (com ou sem `v`): é ela que o app compara para avisar sobre a nova versão.

## Observações

- O build de release usa R8. O youtubedl-android lê o JSON do yt-dlp por reflexão e extrai o Python com o Commons Compress; as regras em `app/proguard-rules.pro` mantêm essas classes. Se um Download funcionar no debug e falhar no release, comece por elas.
- Cada APK tem ~50–60 MB; ~50 MB são o Python e o ffmpeg embutidos.
