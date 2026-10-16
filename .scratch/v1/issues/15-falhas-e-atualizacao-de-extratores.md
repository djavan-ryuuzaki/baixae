# 15: Falhas de Download e atualização de Extratores

**What to build:** Downloads que falham mostram mensagem por tipo: Plataforma mudou ("O Instagram mudou e o app precisa de atualização") com botão "Atualizar extratores e tentar de novo"; Post que exige login ("Este Post exige login e não é suportado"); outros erros. Configurações mostra a versão dos Extratores e permite atualizar. Uma verificação semanal atualiza os Extratores.

**Blocked by:** 04

**Status:** done

- [x] Falha "Plataforma mudou" mostra a mensagem com o nome da Plataforma e o botão de atualizar e tentar de novo
- [x] O botão atualiza o yt-dlp e reenfileira o Download
- [x] Post privado, restrito por idade ou stories mostra a mensagem de login exigido, sem botão de tentar de novo
- [x] Configurações mostra a versão atual dos Extratores e um botão "Atualizar extratores"
- [x] Tarefa periódica semanal atualiza os Extratores
- [x] Nenhuma nova tentativa automática silenciosa
- [x] Testes do núcleo cobrem o mapeamento de erros do Extrator para mensagens e ações

## Comments

- Núcleo: `AcaoAposFalha.para(tipo)`, `FilaDeDownloads.tentarDeNovo` (só por ação do usuário; recusado para "exige login") e `Extratores` (versão, atualizar, atualizar e tentar de novo, verificação semanal idempotente com a data guardada nas Configurações).
- Adaptador: `ClassificadorDeFalhas` traduz a saída do yt-dlp (testado em `ClassificadorDeFalhasTest`); a versão mostrada vem do registro da biblioteca ou, antes da primeira atualização, de `yt-dlp --version`.
- `VerificacaoSemanalWorker`: periódico de 7 dias, com rede e bateria não baixa, e atraso inicial de 1 dia para não disputar CPU com a primeira abertura do app (a preparação do yt-dlp é pesada).
- Verificado no emulador: versão 2025.11.12 (embutida) → "Atualizar" → 2026.08.19; Link de vídeo inexistente → "Falhou" com "Tentar de novo". As mensagens de "Plataforma mudou" e "exige login" foram verificadas só por testes.
- Relato do usuário (Reel público `instagram.com/reel/DekctOWzeZx` → "exige login"): o yt-dlp embutido (2025.11.12) não extrai mais o Instagram e responde "rate-limit reached or login required", que era classificado como login. Correções: essa mensagem passa a ser "Plataforma mudou" (com "Atualizar extratores e tentar de novo"), e a fila atualiza os Extratores antes de baixar quando a última verificação tem mais de 7 dias ou nunca aconteceu. Verificado no emulador com instalação limpa: o Reel baixa direto.
