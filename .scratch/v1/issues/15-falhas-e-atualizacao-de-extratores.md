# 15: Falhas de Download e atualização de Extratores

**What to build:** Downloads que falham mostram mensagem por tipo: Plataforma mudou ("O Instagram mudou e o app precisa de atualização") com botão "Atualizar extratores e tentar de novo"; Post que exige login ("Este Post exige login e não é suportado"); outros erros. Configurações mostra a versão dos Extratores e permite atualizar. Uma verificação semanal atualiza os Extratores.

**Blocked by:** 04

**Status:** ready-for-agent

- [ ] Falha "Plataforma mudou" mostra a mensagem com o nome da Plataforma e o botão de atualizar e tentar de novo
- [ ] O botão atualiza o yt-dlp e reenfileira o Download
- [ ] Post privado, restrito por idade ou stories mostra a mensagem de login exigido, sem botão de tentar de novo
- [ ] Configurações mostra a versão atual dos Extratores e um botão "Atualizar extratores"
- [ ] Tarefa periódica semanal atualiza os Extratores
- [ ] Nenhuma nova tentativa automática silenciosa
- [ ] Testes do núcleo cobrem o mapeamento de erros do Extrator para mensagens e ações
