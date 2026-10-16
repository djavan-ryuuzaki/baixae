# 03: Bibliotecas padrão e metadados do Vídeo

**What to build:** cada Vídeo baixado vai automaticamente para a Biblioteca padrão da sua Plataforma. A aba Bibliotecas mostra a grade das 5 Bibliotecas padrão e, ao abrir uma, seus Vídeos com miniatura, autor, legenda/título, duração e data do Download. Cada Vídeo tem "Abrir original". Introduz a navegação por três abas (Baixar, Bibliotecas, Configurações — esta ainda vazia).

**Blocked by:** 02

**Status:** done

- [x] Na primeira execução existem 5 Bibliotecas padrão, uma por Plataforma
- [x] Um Vídeo baixado aparece na Biblioteca padrão da sua Plataforma
- [x] Cada Vídeo guarda e exibe Plataforma, autor, legenda/título, Link do Post, data do Download, duração e miniatura
- [x] "Abrir original" abre o Link do Post
- [x] Tocar num Vídeo o reproduz no player do sistema
- [x] Dados persistem após fechar e reabrir o app
- [x] Navegação por três abas: Baixar, Bibliotecas, Configurações
- [x] Testes do núcleo cobrem a atribuição à Biblioteca padrão

## Comments

- `Acervo.iniciar()` cria as Bibliotecas padrão (ids determinísticos `padrao-<PLATAFORMA>`); o `Baixador` atribui cada Vídeo à Biblioteca padrão. Miniaturas são geradas do próprio arquivo (MediaMetadataRetriever), para funcionar sem rede e sem depender de URLs de miniatura que expiram. Verificado no emulador: grade com 5 Bibliotecas, lista, detalhe, reprodução no player do sistema e persistência após reiniciar.
