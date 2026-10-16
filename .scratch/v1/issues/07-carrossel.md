# 07: Carrossel (um Post, vários Vídeos)

**What to build:** um Download de um Post com vários vídeos (ex.: carrossel do Instagram) gera um Vídeo para cada vídeo do Post, ignorando fotos, todos na Biblioteca padrão da Plataforma, com sufixos de nome.

**Blocked by:** 06

**Status:** done

- [x] Post com 3 vídeos e 2 fotos gera 3 Vídeos e nenhuma foto
- [x] Os Vídeos recebem nomes com sufixo `_2`, `_3`
- [x] Todos guardam o mesmo Link do Post
- [x] A Fila de Downloads mostra o progresso do Post como um item
- [x] Teste do núcleo cobre o cenário de carrossel com Extrator falso

## Comments

- O `Baixador` já ignora fotos e gera um Vídeo por vídeo do Post; a fila mostra o Post como um item, com progresso agregado. Testes em `CarrosselTest`.
- Adaptador: entradas do JSON do yt-dlp viram mídias (foto quando `vcodec == none` ou extensão de imagem); cada vídeo é baixado com `--playlist-items N`. Adicionado `--no-playlist` para que um Link do YouTube com `&list=` baixe só o vídeo.
- Não verificado com um carrossel real do Instagram (exige um Post público com vários vídeos).
