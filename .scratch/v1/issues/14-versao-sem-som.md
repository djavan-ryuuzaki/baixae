# 14: Versão sem som

**What to build:** na tela de um Vídeo, "Criar versão sem som" gera um novo Vídeo sem o áudio original, na mesma Biblioteca, com nome `<nome> (sem som)`, mantendo o original intacto.

**Blocked by:** 03

**Status:** ready-for-agent

- [ ] Ação gera novo arquivo sem trilha de áudio, sem recodificar o vídeo
- [ ] Novo Vídeo fica na mesma Biblioteca e herda Plataforma, autor, legenda e Link do original
- [ ] Nome `<nome> (sem som)`, com sufixo em caso de colisão
- [ ] Vídeo original permanece inalterado
- [ ] Teste do núcleo cobre a criação com Processador de vídeo falso
