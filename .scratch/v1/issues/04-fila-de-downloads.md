# 04: Fila de Downloads em segundo plano

**What to build:** Downloads entram numa Fila de Downloads visível na aba Baixar, com até 2 simultâneos, que continua com o app fechado e mostra progresso numa notificação. O usuário pode cancelar (arquivo parcial descartado). Sem internet, Downloads ficam "aguardando rede" e retomam sozinhos. Opção "Baixar só no Wi-Fi" em Configurações (desligada por padrão).

**Blocked by:** 01

**Status:** done

- [x] Enfileirar 5 Links resulta em no máximo 2 Downloads em andamento ao mesmo tempo
- [x] A fila continua com o app fechado; notificação mostra progresso
- [x] A aba Baixar lista Downloads por estado: pendente, aguardando rede, em andamento, concluído, falhou, cancelado
- [x] Cancelar um Download em andamento interrompe e apaga o arquivo parcial
- [x] Sem rede, Downloads esperam e retomam quando a rede volta, sem contar como falha
- [x] Com "só Wi-Fi" ligado, Downloads esperam até haver Wi-Fi
- [x] Nenhum Download falho é tentado de novo automaticamente
- [x] Testes do núcleo (Conectividade e Extrator falsos) cobrem concorrência, espera de rede, Wi-Fi e cancelamento

## Comments

- `FilaDeDownloads` no núcleo (máx. 2 simultâneos, espera de rede, só Wi-Fi, cancelamento, sem nova tentativa automática, retomada de itens interrompidos pela morte do processo). Progresso é mantido em memória (`StateFlow`), não persistido a cada atualização.
- Android: `FilaWorker` (WorkManager, serviço em primeiro plano `dataSync`) com notificação de progresso e aviso de conclusão; quando sobram itens aguardando rede, agenda um worker com restrição de rede (`CONNECTED` ou `UNMETERED`).
- Antecipado do ticket 06: nome padrão com sufixos `_2`, `_3` (necessário para Downloads simultâneos do mesmo autor no mesmo dia), com trava para que Downloads simultâneos não disputem o mesmo nome.
- Verificado no emulador: 3 Links → 2 em andamento + 1 pendente; cancelar apagou o `.part`; fila concluiu com o app em segundo plano; sem rede → "Aguardando rede" → retomou ao religar o Wi-Fi.
- Correções da revisão: (1) erro de rede com o celular conectado agora é "Falhou" (antes ia para "Aguardando rede" e o worker com restrição de rede retentava em laço — nova tentativa silenciosa); (2) só o cancelamento pedido pelo usuário vira "Cancelado"; parada do sistema (ex.: fim do serviço em segundo plano) devolve o item para "Pendente"; (3) o nome do Vídeo passa a ser o nome com que o MediaStore de fato gravou o arquivo (ele acrescenta " (1)" quando há um arquivo de outro app, invisível para nós, com o mesmo nome).
