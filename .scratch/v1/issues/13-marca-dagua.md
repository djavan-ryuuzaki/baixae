# 13: Marca d'água no Compartilhamento

**What to build:** opção em Configurações para ligar a Marca d'água (desligada por padrão). Ligada, compartilhar um Vídeo gera uma cópia com o texto "Baixado com SocialVideoDownloader" no canto inferior direito, semitransparente, mostrando progresso; o arquivo da Biblioteca nunca é alterado.

**Blocked by:** 12

**Status:** ready-for-agent

- [ ] Configurações tem a opção Marca d'água, desligada por padrão
- [ ] Desligada: compartilha o arquivo original
- [ ] Ligada: gera cópia marcada (texto fixo, canto inferior direito, semitransparente) e compartilha a cópia
- [ ] Progresso visível durante a geração; é possível cancelar
- [ ] Arquivo da Biblioteca permanece idêntico após o Compartilhamento
- [ ] Cópias temporárias são limpas depois
- [ ] Teste do núcleo cobre a decisão original × cópia marcada com Processador de vídeo falso
