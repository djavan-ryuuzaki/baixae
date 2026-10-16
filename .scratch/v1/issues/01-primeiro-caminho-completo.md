# 01: Primeiro caminho completo (colar Link do YouTube → Vídeo baixado)

**What to build:** o usuário abre o app, cola um Link do YouTube na aba Baixar, toca em baixar e o vídeo é salvo em Movies/SocialVideoDownloader; o Vídeo aparece numa lista simples. Estabelece o projeto Android (Kotlin + Compose, API 26+), o núcleo Kotlin puro com suas portas (Extrator, Armazenamento de arquivos, Repositório, Relógio), o adaptador youtubedl-android (com QuickJS) e o padrão de testes no núcleo com fakes. Ver spec `.scratch/v1/spec.md` e ADR-0002.

**Blocked by:** None (can start immediately)

**Status:** done

- [x] Projeto Android compila e instala em Android 8.0+; núcleo é um módulo Kotlin/JVM sem dependências Android
- [x] Colar um Link público do YouTube e tocar em baixar gera um arquivo .mp4 em Movies/SocialVideoDownloader, visível na galeria
- [x] O Vídeo aparece numa lista na tela com seu nome
- [x] O Extrator real está atrás da porta Extrator; o núcleo não conhece youtubedl-android
- [x] Teste do núcleo com Extrator e Armazenamento falsos cobre "Link → Vídeo registrado com arquivo salvo"
- [x] Textos da interface em recursos de string pt-BR

## Comments

- Implementado: módulo `core` (Kotlin/JVM puro) com portas `Extrator`, `ArmazenamentoDeArquivos`, `Repositorio`, `Relogio`; app Android com Room, MediaStore e youtubedl-android 0.18.1 (QuickJS embutido no próprio AAR). Verificado no emulador API 35: `https://youtu.be/jNQXAC9IVRw` gerou `Movies/SocialVideoDownloader/youtube_jawed_<data>.mp4` registrado no MediaStore.
