# 16: Limite de qualidade e espaço livre

**What to build:** Downloads usam a melhor qualidade até o limite definido em Configurações (padrão 1080p). Antes de começar, o app verifica se há espaço livre suficiente e avisa se faltar. Sem limite de duração.

**Blocked by:** 04

**Status:** ready-for-agent

- [ ] Configurações oferece limite de qualidade (480p, 720p, 1080p, máxima), padrão 1080p
- [ ] Vídeo disponível só em 4K com limite 1080p baixa a melhor versão até 1080p
- [ ] Sem espaço suficiente (pelo tamanho estimado), o Download falha antes de começar com aviso claro
- [ ] Vídeos longos não têm limite de duração
- [ ] Testes do núcleo cobrem o limite de qualidade repassado ao Extrator e a checagem de espaço
