# 12: Compartilhar Vídeo a partir do app

**What to build:** na tela de um Vídeo, "Compartilhar" abre o menu Compartilhar padrão do Android para enviar o arquivo a qualquer app.

**Blocked by:** 03

**Status:** done

- [x] "Compartilhar" abre o menu do sistema com o arquivo do Vídeo
- [ ] Envio para WhatsApp e Instagram funciona (verificação manual)
- [x] Arquivo ausente não pode ser compartilhado

## Comments

- Núcleo: `Compartilhamento.preparar(videoId)` devolve o arquivo a enviar e recusa (marcando) Arquivo ausente. Android: `ACTION_SEND` com `ClipData` para a permissão de leitura chegar ao app escolhido; `FileProvider` (cache `compartilhar/`) já preparado para a cópia com Marca d'água do #13.
- Verificado no emulador: o menu do sistema abre com o arquivo do Vídeo.
- Pendente (manual): enviar para WhatsApp e Instagram num celular com esses apps.
