# Extração no celular com yt-dlp embutido

A extração dos vídeos roda no próprio celular, com yt-dlp (Python + ffmpeg) embutido no app, e o usuário pode atualizar os extratores pelo app. As Plataformas mudam com frequência e o yt-dlp acompanha essas mudanças; um servidor próprio traria custo mensal e responsabilidade jurídica sobre o conteúdo trafegado.

## Considered Options

- **Servidor próprio rodando yt-dlp**: rejeitado por custo e responsabilidade jurídica.
- **API paga de terceiros**: rejeitado por custo e dependência de fornecedor.

## Consequences

- O APK cresce ~35–45 MB por ABI (Python + ffmpeg); publicar APKs separados por ABI.
- O YouTube exige um runtime JavaScript junto ao yt-dlp.
- Threads não tem extrator no yt-dlp (pedido aberto desde 2023); ver ADR-0004.
- O wrapper Android escolhido é o youtubedl-android, parado desde nov/2025. Ele fica isolado atrás de uma interface interna de Extrator, para poder ser trocado (ex.: pelo runtime do YTDLnis) sem tocar no resto do app. O yt-dlp em si continua atualizável em tempo de execução.
- O youtubedl-android é GPL-3.0, então o app também é distribuído como GPL-3.0 (gratuito e de código aberto).
