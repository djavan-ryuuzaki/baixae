# 17: Aviso de nova versão do app

**What to build:** junto com a verificação semanal de Extratores, o app consulta o GitHub Releases do projeto e, havendo versão mais nova, mostra "Nova versão disponível" com link para o APK. A instalação fica com o usuário.

**Blocked by:** 15

**Status:** ready-for-agent

- [ ] Verificação semanal compara a versão instalada com a última release do GitHub
- [ ] Havendo versão nova, aparece aviso com link para a página da release
- [ ] Sem versão nova ou sem rede, nada é exibido
- [ ] Configurações tem "Verificar atualizações agora"
- [ ] Teste do núcleo com Verificador de versões falso cobre a comparação de versões
