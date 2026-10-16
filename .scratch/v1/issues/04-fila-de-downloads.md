# 04: Fila de Downloads em segundo plano

**What to build:** Downloads entram numa Fila de Downloads visível na aba Baixar, com até 2 simultâneos, que continua com o app fechado e mostra progresso numa notificação. O usuário pode cancelar (arquivo parcial descartado). Sem internet, Downloads ficam "aguardando rede" e retomam sozinhos. Opção "Baixar só no Wi-Fi" em Configurações (desligada por padrão).

**Blocked by:** 01

**Status:** ready-for-agent

- [ ] Enfileirar 5 Links resulta em no máximo 2 Downloads em andamento ao mesmo tempo
- [ ] A fila continua com o app fechado; notificação mostra progresso
- [ ] A aba Baixar lista Downloads por estado: pendente, aguardando rede, em andamento, concluído, falhou, cancelado
- [ ] Cancelar um Download em andamento interrompe e apaga o arquivo parcial
- [ ] Sem rede, Downloads esperam e retomam quando a rede volta, sem contar como falha
- [ ] Com "só Wi-Fi" ligado, Downloads esperam até haver Wi-Fi
- [ ] Nenhum Download falho é tentado de novo automaticamente
- [ ] Testes do núcleo (Conectividade e Extrator falsos) cobrem concorrência, espera de rede, Wi-Fi e cancelamento
