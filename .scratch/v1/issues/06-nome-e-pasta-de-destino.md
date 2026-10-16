# 06: Nome do Vídeo e Pasta de destino

**What to build:** Vídeos recebem o nome padrão `{plataforma}_{autor}_{data}` com sufixo `_2`, `_3` em colisões. O usuário pode renomear um Vídeo (o arquivo é renomeado junto; nome já existente é recusado). Em Configurações, define a Pasta de destino padrão (Movies/SocialVideoDownloader); na aba Baixar, pode trocá-la para um Download específico.

**Blocked by:** 03

**Status:** ready-for-agent

- [ ] Novo Vídeo recebe nome `{plataforma}_{autor}_{data}` com caracteres inválidos sanitizados
- [ ] Dois Vídeos com mesmo nome padrão na mesma pasta recebem `_2`, `_3`
- [ ] Renomear no app renomeia o arquivo no celular
- [ ] Renomear para um nome já existente na pasta é recusado com aviso
- [ ] Configurações permite escolher a Pasta de destino padrão
- [ ] A aba Baixar permite trocar a Pasta de destino só para aquele Download
- [ ] Downloads vindos do menu Compartilhar usam a Pasta de destino padrão
- [ ] Testes do núcleo cobrem nome padrão, sufixos e recusa de renomear
