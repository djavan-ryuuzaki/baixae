# 16: Limite de qualidade e espaço livre

**What to build:** Downloads usam a melhor qualidade até o limite definido em Configurações (padrão 1080p). Antes de começar, o app verifica se há espaço livre suficiente e avisa se faltar. Sem limite de duração.

**Blocked by:** 04

**Status:** done

- [x] Configurações oferece limite de qualidade (480p, 720p, 1080p, máxima), padrão 1080p
- [x] Vídeo disponível só em 4K com limite 1080p baixa a melhor versão até 1080p
- [x] Sem espaço suficiente (pelo tamanho estimado), o Download falha antes de começar com aviso claro
- [x] Vídeos longos não têm limite de duração
- [x] Testes do núcleo cobrem o limite de qualidade repassado ao Extrator e a checagem de espaço

## Comments

- Núcleo: o limite das Configurações vai para `obterPost` e `baixar`; antes do primeiro vídeo, o `Baixador` compara o dobro do tamanho estimado (temporário + cópia na Pasta de destino) com o espaço livre e falha com `SEM_ESPACO`. Tamanho desconhecido não bloqueia. Nenhum limite de duração.
- Adaptador: o tamanho estimado já considera o limite (`-f` no `--dump-single-json`, com nova leitura sem `-f` se a seleção falhar, ex.: fotos de carrossel). Espaço livre = menor entre cache e armazenamento compartilhado.
- Verificado no emulador: com "Até 480p", um vídeo com versão 4K foi salvo em 854×480. A checagem de espaço foi verificada só por testes.
- Correção: o limite era aplicado à altura, o que rebaixava vídeos verticais (um Reel 1080×1920 saía em 540×960 com "1080p"). Agora vale o lado menor (`-S res:N` do yt-dlp): o Reel sai em 1080×1920 e o vídeo 4K com "Até 480p" continua em 854×480.
