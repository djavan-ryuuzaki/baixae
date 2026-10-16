# 18: Publicação no GitHub Releases

**What to build:** o projeto é publicado como software livre GPL-3.0, com APKs assinados separados por ABI (arm64-v8a, armeabi-v7a, x86_64) gerados por um build de release e anexados ao GitHub Releases. Ver ADR-0001.

**Blocked by:** 01

**Status:** done

- [x] Arquivo de licença GPL-3.0 e avisos das bibliotecas de terceiros (yt-dlp, Python, ffmpeg)
- [x] Build de release gera um APK assinado por ABI
- [x] README em pt-BR explica a instalação fora da Play Store e as Plataformas suportadas
- [x] Processo documentado para criar uma release no GitHub com os APKs

## Comments

- `LICENSE` (texto oficial da GPL-3.0), `THIRD_PARTY_NOTICES.md`, `README.md` em pt-BR e `docs/publicacao.md` (chave, `keystore.properties`, versão, `gh release create`).
- Build de release: um APK por ABI (armeabi-v7a, arm64-v8a, x86_64) com `versionCode * 10 + n`; assinatura lida de `keystore.properties` (fora do git); R8 ligado com regras para youtubedl-android (Jackson) e Commons Compress — sem a segunda, a extração do Python quebrava no release.
- Verificado com uma chave de teste: 3 APKs assinados (49–59 MB); o APK x86_64 de release no emulador abriu em ~0,5 s e baixou, criou versão sem som e compartilhou com Marca d'água.
- O teste do release revelou um defeito na fila: um `Error` lançado pelo Extrator deixava o item "Baixando…" para sempre. Corrigido no núcleo (com teste) — o item agora fica "Falhou".
- Pendente (humano): criar a chave definitiva, o repositório no GitHub, preencher `repositorioGithub` e publicar a primeira release.
