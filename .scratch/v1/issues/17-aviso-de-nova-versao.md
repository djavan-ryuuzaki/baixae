# 17: Aviso de nova versão do app

**What to build:** junto com a verificação semanal de Extratores, o app consulta o GitHub Releases do projeto e, havendo versão mais nova, mostra "Nova versão disponível" com link para o APK. A instalação fica com o usuário.

**Blocked by:** 15

**Status:** done

- [x] Verificação semanal compara a versão instalada com a última release do GitHub
- [x] Havendo versão nova, aparece aviso com link para a página da release
- [x] Sem versão nova ou sem rede, nada é exibido
- [x] Configurações tem "Verificar atualizações agora"
- [x] Teste do núcleo com Verificador de versões falso cobre a comparação de versões

## Comments

- Núcleo: `Versao.maisNova`, `AtualizacoesDoApp` (silenciosa sem versão nova ou sem rede; aviso some quando a versão instalada alcança a publicada) e `VerificacaoSemanal`, que roda Extratores e versão do app na mesma tarefa.
- Adaptador: `VerificadorGithub` consulta `releases/latest` da API do GitHub. O repositório vem da propriedade Gradle `repositorioGithub` (em `gradle.properties`, hoje vazia — com ela vazia o aviso fica desligado). **Preencher com `dono/SocialVideoDownloader` quando o repositório existir.**
- Interface: card "Nova versão disponível" com link para a página da release em Configurações, notificação quando a tarefa semanal encontra uma versão nova e "Verificar atualizações agora".
- Verificado no emulador compilando com `-PrepositorioGithub=yt-dlp/yt-dlp`: apareceu "Nova versão disponível: 2026.08.19" com o link; um repositório sem Releases não mostrou nada.
