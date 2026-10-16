# 09: Bibliotecas personalizadas e mover Vídeos

**What to build:** o usuário cria, renomeia e apaga Bibliotecas personalizadas e move um ou vários Vídeos (seleção por toque longo) entre Bibliotecas. Apagar uma Biblioteca personalizada devolve seus Vídeos à Biblioteca padrão da Plataforma de origem. Bibliotecas padrão não podem ser apagadas nem renomeadas. Mover não mexe no arquivo do celular. É possível filtrar por Plataforma de origem.

**Blocked by:** 03

**Status:** done

- [x] Criar Biblioteca personalizada com nome; nomes repetidos são recusados
- [x] Renomear Biblioteca personalizada; Bibliotecas padrão não oferecem renomear nem apagar
- [x] Selecionar vários Vídeos por toque longo e movê-los para outra Biblioteca
- [x] Mover não altera o caminho do arquivo
- [x] Apagar Biblioteca personalizada devolve cada Vídeo à Biblioteca padrão da sua Plataforma
- [x] Filtro por Plataforma de origem funciona dentro de qualquer Biblioteca
- [x] Testes do núcleo cobrem mover, apagar e proteção das Bibliotecas padrão

## Comments

- Núcleo: `Acervo.criarBiblioteca/renomearBiblioteca/apagarBiblioteca/mover` e `videosDa(biblioteca, plataforma)`. Nomes de Biblioteca são únicos sem diferenciar maiúsculas (inclusive em relação às padrão).
- Interface: "+" na grade cria; Bibliotecas personalizadas mostram Renomear/Apagar (padrão não mostram); toque longo seleciona, "Mover" abre a lista de destinos; chips de Plataforma aparecem quando a Biblioteca tem Vídeos de mais de uma.
- Verificado no emulador: criar "Receitas", mover 2 Vídeos do YouTube, apagar "Receitas" → YouTube volta a ter 5.
