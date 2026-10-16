# 18: Publicação no GitHub Releases

**What to build:** o projeto é publicado como software livre GPL-3.0, com APKs assinados separados por ABI (arm64-v8a, armeabi-v7a, x86_64) gerados por um build de release e anexados ao GitHub Releases. Ver ADR-0001.

**Blocked by:** 01

**Status:** ready-for-agent

- [ ] Arquivo de licença GPL-3.0 e avisos das bibliotecas de terceiros (yt-dlp, Python, ffmpeg)
- [ ] Build de release gera um APK assinado por ABI
- [ ] README em pt-BR explica a instalação fora da Play Store e as Plataformas suportadas
- [ ] Processo documentado para criar uma release no GitHub com os APKs
