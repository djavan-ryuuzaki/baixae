# 13: Marca d'água no Compartilhamento

**What to build:** opção em Configurações para ligar a Marca d'água (desligada por padrão). Ligada, compartilhar um Vídeo gera uma cópia com o texto "Baixado com SocialVideoDownloader" no canto inferior direito, semitransparente, mostrando progresso; o arquivo da Biblioteca nunca é alterado.

**Blocked by:** 12

**Status:** done

- [x] Configurações tem a opção Marca d'água, desligada por padrão
- [x] Desligada: compartilha o arquivo original
- [x] Ligada: gera cópia marcada (texto fixo, canto inferior direito, semitransparente) e compartilha a cópia
- [x] Progresso visível durante a geração; é possível cancelar
- [x] Arquivo da Biblioteca permanece idêntico após o Compartilhamento
- [x] Cópias temporárias são limpas depois
- [x] Teste do núcleo cobre a decisão original × cópia marcada com Processador de vídeo falso

## Comments

- Núcleo: `Compartilhamento` com a porta `ProcessadorDeVideo`; com a Marca d’água ligada gera uma cópia temporária, informa progresso, descarta a cópia se cancelado e limpa a cópia anterior no próximo Compartilhamento (`encerrar()`); os temporários também são apagados ao iniciar o processo.
- Adaptador: Media3 Transformer 1.11.1 com `TextOverlay` (H.264). O `TextOverlay` usa um tamanho fixo em pixels, então o tamanho do texto é calculado a partir do lado menor do vídeo (4,5%).
- Verificado no emulador: cópia gerada com o texto no canto inferior direito, semitransparente (quadro extraído com ffmpeg); o MD5 do arquivo da Biblioteca não mudou.
