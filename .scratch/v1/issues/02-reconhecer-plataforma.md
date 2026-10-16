# 02: Reconhecer a Plataforma do Link

**What to build:** ao colar um Link, o app reconhece a Plataforma (Instagram, Facebook, X, Bluesky, YouTube) e baixa por ela; Links de outros domínios são recusados com a lista de Plataformas suportadas. Facebook aparece marcado como Plataforma instável.

**Blocked by:** 01

**Status:** ready-for-agent

- [ ] Links de instagram.com, facebook.com/fb.watch, twitter.com/x.com, bsky.app, youtube.com/youtu.be (incl. Shorts) são reconhecidos com a Plataforma correta
- [ ] twitter.com e x.com resultam na Plataforma X
- [ ] Links de outros domínios (ex.: TikTok, Threads) são recusados com mensagem listando as Plataformas suportadas; o extrator genérico nunca é usado
- [ ] A lista de Plataformas suportadas mostra o Facebook como instável
- [ ] Reconhecimento é lógica pura do núcleo, coberta por testes
- [ ] Um Download de cada Plataforma funciona com Link público real (verificação manual)
