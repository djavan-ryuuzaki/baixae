# 08: Sugestão de Link copiado e Link repetido

**What to build:** ao abrir o app com um Link suportado na área de transferência, aparece a sugestão "Baixar o Link copiado?" que inicia o Download com um toque — sem preencher nem baixar sozinho. Ao pedir um Link já baixado, o app avisa "Você já baixou isto (Biblioteca X)" com opções Abrir e Baixar de novo.

**Blocked by:** 03

**Status:** done

- [x] Com Link suportado copiado, abrir o app mostra a sugestão; tocar inicia o Download
- [x] A área de transferência só é lida com o app em primeiro plano; nada é baixado sem toque
- [x] Link não suportado na área de transferência não gera sugestão
- [x] Link já baixado (por colar, sugestão ou menu Compartilhar) mostra o aviso com a Biblioteca atual
- [x] "Abrir" leva ao Vídeo; "Baixar de novo" enfileira normalmente
- [x] Teste do núcleo cobre detecção de Link repetido

## Comments

- Núcleo: `Acervo.downloadAnterior` compara Links pela `ChaveDoLink` (ignora `www.`/`m.`, barra final, parâmetros de rastreio; `youtu.be`, `watch?v=` e `shorts/` do mesmo vídeo são o mesmo Link). `Plataforma.sugestaoDeDownload` decide a sugestão.
- A área de transferência só é lida quando a janela do app ganha foco e o clipe mudou (pela data do clipe); nada é preenchido nem baixado sem toque.
- Menu Compartilhar: Link repetido abre um diálogo sobre o app de origem com Abrir / Baixar de novo.
- Verificado no emulador: sugestão aparece para Link do YouTube copiado (e não para TikTok); Link repetido mostra "Você já baixou isto" com a Biblioteca, por sugestão e pelo menu Compartilhar; "Abrir" leva ao Vídeo.
