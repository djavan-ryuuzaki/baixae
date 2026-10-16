# 08: Sugestão de Link copiado e Link repetido

**What to build:** ao abrir o app com um Link suportado na área de transferência, aparece a sugestão "Baixar o Link copiado?" que inicia o Download com um toque — sem preencher nem baixar sozinho. Ao pedir um Link já baixado, o app avisa "Você já baixou isto (Biblioteca X)" com opções Abrir e Baixar de novo.

**Blocked by:** 03

**Status:** ready-for-agent

- [ ] Com Link suportado copiado, abrir o app mostra a sugestão; tocar inicia o Download
- [ ] A área de transferência só é lida com o app em primeiro plano; nada é baixado sem toque
- [ ] Link não suportado na área de transferência não gera sugestão
- [ ] Link já baixado (por colar, sugestão ou menu Compartilhar) mostra o aviso com a Biblioteca atual
- [ ] "Abrir" leva ao Vídeo; "Baixar de novo" enfileira normalmente
- [ ] Teste do núcleo cobre detecção de Link repetido
