# 11: Busca e ordenação na Biblioteca

**What to build:** dentro de uma Biblioteca, Vídeos aparecem do Download mais recente para o mais antigo, e o usuário pode buscar por nome, autor e legenda.

**Blocked by:** 03

**Status:** done

- [x] Ordem padrão: data do Download decrescente
- [x] Busca filtra por nome, autor e legenda, sem diferenciar maiúsculas nem acentos
- [x] Busca combinável com o filtro por Plataforma (quando 09 estiver pronto)
- [x] Testes do núcleo cobrem ordenação e busca

## Comments

- `ConsultaDeVideos` (núcleo) concentra filtro por Plataforma, busca (nome, autor e legenda, sem maiúsculas nem acentos) e ordem por data do Download decrescente; a tela de Biblioteca e `Acervo.videosDa` usam a mesma regra.
- Verificado no emulador: buscar "ZOO" na Biblioteca YouTube deixa só "Me at the zoo".
