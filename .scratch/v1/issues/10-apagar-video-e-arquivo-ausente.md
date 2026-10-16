# 10: Apagar Vídeo e Arquivo ausente

**What to build:** apagar um Vídeo pede confirmação com "Apagar também o arquivo do celular" marcado por padrão. Vídeos cujo arquivo foi apagado ou movido fora do app aparecem como Arquivo ausente, com opção de removê-los da Biblioteca.

**Blocked by:** 03

**Status:** done

- [x] Confirmação de apagar com a caixa marcada por padrão
- [x] Com a caixa marcada, Vídeo e arquivo são apagados; desmarcada, só o Vídeo sai do app
- [x] Ao abrir uma Biblioteca, Vídeos sem arquivo aparecem como Arquivo ausente
- [x] Arquivo ausente não pode ser reproduzido nem compartilhado; oferece "Remover da Biblioteca"
- [x] Testes do núcleo cobrem os dois modos de apagar e a detecção de Arquivo ausente

## Comments

- Núcleo: `Acervo.apagarVideo(id, apagarArquivo)` e `Acervo.verificarArquivos(biblioteca)` (marca e desmarca `Video.arquivoAusente`). Chamado ao abrir uma Biblioteca e ao abrir um Vídeo.
- Adaptador: "existe" tenta abrir o arquivo (não só consultar a linha do MediaStore), para detectar também arquivos apagados por fora do MediaStore.
- Arquivo ausente esconde Reproduzir e Renomear; o Compartilhar (#12) também fica bloqueado.
- Verificado no emulador: arquivo removido via `rm` → "Arquivo ausente" na lista e no detalhe; "Remover da Biblioteca"; apagar com a caixa marcada (padrão) apagou o arquivo.
