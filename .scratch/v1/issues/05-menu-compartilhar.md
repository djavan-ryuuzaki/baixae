# 05: Receber Links pelo menu Compartilhar

**What to build:** o app aparece como destino no menu Compartilhar do Android. Ao compartilhar um Post de uma Plataforma para o app, aparece só um aviso "Adicionado à fila" e o usuário continua no app de origem; o Download segue na Fila de Downloads usando a Pasta de destino padrão. Ver ADR-0003.

**Blocked by:** 04

**Status:** ready-for-agent

- [ ] Compartilhar um Post do Instagram/YouTube/X/Bluesky/Facebook mostra o app como opção
- [ ] Escolher o app adiciona o Download à fila e mostra "Adicionado à fila" sem abrir a interface completa
- [ ] O usuário permanece no app de origem
- [ ] Texto compartilhado que contém um Link no meio de outras palavras é aceito (Link extraído)
- [ ] Link não suportado compartilhado mostra aviso de recusa, sem enfileirar
