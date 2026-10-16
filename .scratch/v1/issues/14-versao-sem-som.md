# 14: Versão sem som

**What to build:** na tela de um Vídeo, "Criar versão sem som" gera um novo Vídeo sem o áudio original, na mesma Biblioteca, com nome `<nome> (sem som)`, mantendo o original intacto.

**Blocked by:** 03

**Status:** done

- [x] Ação gera novo arquivo sem trilha de áudio, sem recodificar o vídeo
- [x] Novo Vídeo fica na mesma Biblioteca e herda Plataforma, autor, legenda e Link do original
- [x] Nome `<nome> (sem som)`, com sufixo em caso de colisão
- [x] Vídeo original permanece inalterado
- [x] Teste do núcleo cobre a criação com Processador de vídeo falso

## Comments

- Núcleo: `VersaoSemSom.criar` (novo Vídeo com `origemId`, mesma Biblioteca e pasta, data de criação como data do Download para aparecer no topo). A reserva de nomes passou para `NomeDeVideo.salvarComNomeLivre`, compartilhada com o `Baixador`, para que Downloads e Versões sem som simultâneos não disputem o mesmo nome.
- Adaptador: `MediaExtractor` + `MediaMuxer` copiando só a trilha de vídeo (preserva a rotação).
- Verificado no emulador com ffprobe: a versão sem som tem só a trilha H.264, com os mesmos 284 quadros (sem recodificação); o MD5 do original não mudou.
