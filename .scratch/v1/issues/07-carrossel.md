# 07: Carrossel (um Post, vários Vídeos)

**What to build:** um Download de um Post com vários vídeos (ex.: carrossel do Instagram) gera um Vídeo para cada vídeo do Post, ignorando fotos, todos na Biblioteca padrão da Plataforma, com sufixos de nome.

**Blocked by:** 06

**Status:** ready-for-agent

- [ ] Post com 3 vídeos e 2 fotos gera 3 Vídeos e nenhuma foto
- [ ] Os Vídeos recebem nomes com sufixo `_2`, `_3`
- [ ] Todos guardam o mesmo Link do Post
- [ ] A Fila de Downloads mostra o progresso do Post como um item
- [ ] Teste do núcleo cobre o cenário de carrossel com Extrator falso
