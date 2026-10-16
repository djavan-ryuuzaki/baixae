# SocialVideoDownloader

App Android, gratuito e de código aberto, para baixar vídeos públicos do **Instagram**, **Facebook**, **X** (antigo Twitter), **Bluesky** e **YouTube** e organizá-los em Bibliotecas dentro do app.

- Cole um Link na aba **Baixar** ou compartilhe um Post para o app pelo menu **Compartilhar** de qualquer app.
- Os Downloads entram numa **Fila de Downloads** (até 2 ao mesmo tempo) que continua com o app fechado.
- Cada Vídeo vai para a **Biblioteca** da sua Plataforma; você pode criar Bibliotecas suas, mover, renomear, apagar e buscar Vídeos.
- **Compartilhe** Vídeos com qualquer app, opcionalmente com a Marca d'água "Baixado com SocialVideoDownloader".
- Crie uma **versão sem som** de um Vídeo, sem perder o original.

A extração roda no próprio celular, com o [yt-dlp](https://github.com/yt-dlp/yt-dlp). Nenhum servidor nosso participa dos Downloads.

## Plataformas suportadas

| Plataforma | Observação |
| --- | --- |
| Instagram | Posts, Reels e carrosséis públicos (só os vídeos; fotos são ignoradas) |
| Facebook | **Instável**: Downloads falham com frequência |
| X | Links de `x.com` e `twitter.com` |
| Bluesky | |
| YouTube | Vídeos, Shorts e lives já encerradas |

Não são suportados: Threads (o yt-dlp ainda não tem extrator), Posts privados, restritos por idade e stories (exigem login).

## Instalação

O app **não está na Google Play**: as regras da loja não permitem apps que baixam vídeos dessas Plataformas (veja [ADR-0001](docs/adr/0001-distribuicao-fora-da-play-store.md)). Ele é distribuído como APK pelo GitHub Releases.

1. Descubra a arquitetura do seu celular. Quase todos os celulares atuais são **arm64-v8a**; aparelhos antigos ou muito simples podem ser **armeabi-v7a**. Apps como "CPU-Z" ou "Droid Hardware Info" mostram isso.
2. Na página de [Releases](../../releases), baixe o APK da sua arquitetura:
   - `SocialVideoDownloader-<versão>-arm64-v8a.apk` — celulares atuais (64 bits)
   - `SocialVideoDownloader-<versão>-armeabi-v7a.apk` — celulares antigos (32 bits)
   - `SocialVideoDownloader-<versão>-x86_64.apk` — emuladores e Chromebooks
3. Abra o APK no celular. Na primeira vez, o Android pede para **permitir a instalação de apps desta fonte** (navegador ou gerenciador de arquivos); permita e volte para instalar.
4. Requer Android 8.0 ou mais novo.

Para atualizar, instale o APK da versão nova por cima da antiga; seus Vídeos e Bibliotecas são mantidos. O próprio app avisa quando há uma versão nova.

## Quando um Download falha

As Plataformas mudam com frequência. Se aparecer "O Instagram mudou e o app precisa de atualização", toque em **Atualizar extratores e tentar de novo**. O app também atualiza os extratores sozinho uma vez por semana, e você pode atualizar em **Configurações → Extratores**.

## Desenvolvimento

- `core/`: regras do app em Kotlin puro (sem Android), com testes: `./gradlew :core:test`.
- `app/`: app Android (Jetpack Compose), que liga o núcleo ao yt-dlp ([youtubedl-android](https://github.com/yausername/youtubedl-android)), ao MediaStore, ao Room e ao WorkManager.
- Vocabulário do domínio em [`CONTEXT.md`](CONTEXT.md); decisões em [`docs/adr/`](docs/adr/).
- Como publicar uma versão: [`docs/publicacao.md`](docs/publicacao.md).

## Licença

[GPL-3.0](LICENSE). Componentes de terceiros e suas licenças: [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md).

Baixe apenas conteúdo que você tem direito de guardar. Respeite os direitos dos autores e os termos de uso de cada Plataforma.
