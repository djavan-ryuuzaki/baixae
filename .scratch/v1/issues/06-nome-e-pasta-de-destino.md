# 06: Nome do Vídeo e Pasta de destino

**What to build:** Vídeos recebem o nome padrão `{plataforma}_{autor}_{data}` com sufixo `_2`, `_3` em colisões. O usuário pode renomear um Vídeo (o arquivo é renomeado junto; nome já existente é recusado). Em Configurações, define a Pasta de destino padrão (Movies/SocialVideoDownloader); na aba Baixar, pode trocá-la para um Download específico.

**Blocked by:** 03

**Status:** done

- [x] Novo Vídeo recebe nome `{plataforma}_{autor}_{data}` com caracteres inválidos sanitizados
- [x] Dois Vídeos com mesmo nome padrão na mesma pasta recebem `_2`, `_3`
- [x] Renomear no app renomeia o arquivo no celular
- [x] Renomear para um nome já existente na pasta é recusado com aviso
- [x] Configurações permite escolher a Pasta de destino padrão
- [x] A aba Baixar permite trocar a Pasta de destino só para aquele Download
- [x] Downloads vindos do menu Compartilhar usam a Pasta de destino padrão
- [x] Testes do núcleo cobrem nome padrão, sufixos e recusa de renomear

## Comments

- Nome padrão e sufixos já entraram no #04. Aqui: `Acervo.renomear` (sanitiza, recusa vazio e nome existente na pasta, renomeia o arquivo via MediaStore), `PastaDeDestino.validar` e as telas.
- Decisão: a Pasta de destino é um caminho relativo dentro de `Movies/`, `DCIM/` ou `Pictures/` (pastas em que o MediaStore aceita vídeos de apps sem permissão especial), digitado num diálogo — não um seletor de pastas do sistema (SAF). Um seletor SAF exigiria um segundo caminho de gravação (DocumentFile) e não deixaria o vídeo indexado na galeria do mesmo jeito.
- Verificado no emulador: renomear muda o arquivo em `Movies/SocialVideoDownloader`; nome repetido mostra "Já existe um arquivo com esse nome nesta pasta."; "Trocar" na aba Baixar salvou em `Movies/Excecao` só naquele Download.
