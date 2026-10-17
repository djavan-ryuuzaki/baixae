# Baixaê v1

**Status:** ready-for-agent

## Problem Statement

Quem consome vídeos no Instagram, Facebook, X, Bluesky e YouTube não tem um jeito simples de guardar esses vídeos no celular. Cada Plataforma esconde o arquivo, os sites de "download" são cheios de anúncios e golpes, e os vídeos baixados acabam espalhados na galeria sem saber de onde vieram. Também não há como reorganizar, renomear ou repassar esses vídeos de forma prática.

## Solution

Um app Android (APK distribuído pelo GitHub, fora da Play Store) que recebe um Link — colado ou enviado pelo menu Compartilhar de qualquer app — reconhece a Plataforma, baixa o(s) vídeo(s) do Post no próprio celular e os registra como Vídeos numa Biblioteca. Cada Plataforma tem sua Biblioteca padrão; o usuário pode criar Bibliotecas personalizadas, mover, renomear, apagar, gerar uma Versão sem som e compartilhar Vídeos, opcionalmente com Marca d'água.

O vocabulário segue `CONTEXT.md`. Decisões estruturais: ADR-0001 (distribuição fora da Play Store), ADR-0002 (extração no celular com yt-dlp), ADR-0003 (menu Compartilhar em vez de botão sobreposto), ADR-0004 (Threads fora da v1).

## User Stories

### Iniciar um Download

1. Como usuário, quero colar um Link na aba Baixar, para baixar um vídeo que vi numa Plataforma.
2. Como usuário, quero que o app reconheça sozinho a Plataforma do Link, para não precisar escolher manualmente.
3. Como usuário, quero que Links de twitter.com e x.com sejam reconhecidos como X, para não depender de qual endereço copiei.
4. Como usuário, quero que um Link de fora das Plataformas seja recusado com a lista de Plataformas suportadas, para entender o que o app aceita.
5. Como usuário, quero compartilhar um Post do Instagram (ou outra Plataforma) diretamente para o app pelo menu Compartilhar, para baixar sem sair do app em que estou.
6. Como usuário, quero ver apenas um aviso "Adicionado à fila" ao compartilhar para o app, para continuar navegando na Plataforma.
7. Como usuário, quero que o app sugira "Baixar o Link copiado?" ao abrir, quando houver um Link na área de transferência, para iniciar com um toque.
8. Como usuário, quero que o app não leia nem preencha a área de transferência sem eu pedir, para preservar minha privacidade.
9. Como usuário, quero ser avisado quando o Link já foi baixado (com a Biblioteca onde está), para evitar duplicatas.
10. Como usuário, quero poder escolher "Abrir" ou "Baixar de novo" para um Link repetido, para decidir caso a caso.
11. Como usuário, quero ver a Plataforma Facebook marcada como instável, para saber que Downloads dela podem falhar.

### Fila de Downloads

12. Como usuário, quero enfileirar vários Downloads seguidos, para não esperar um terminar para pedir o próximo.
13. Como usuário, quero que até 2 Downloads rodem ao mesmo tempo, para que a fila ande rápido sem travar o celular.
14. Como usuário, quero que a fila continue com o app fechado, para não precisar ficar com ele aberto.
15. Como usuário, quero ver o progresso numa notificação, para acompanhar de fora do app.
16. Como usuário, quero ver a Fila de Downloads na aba Baixar, para saber o que está pendente, em andamento ou falhou.
17. Como usuário, quero cancelar um Download em andamento, descartando o arquivo parcial, para desistir de vídeos grandes.
18. Como usuário, quero que a fila espere quando não houver internet e retome sozinha quando a conexão voltar, para não perder pedidos feitos sem sinal.
19. Como usuário, quero uma opção "Baixar só no Wi-Fi" (desligada por padrão), para economizar dados móveis.

### Conteúdo baixado

20. Como usuário, quero que o app baixe a melhor qualidade até 1080p automaticamente, para não precisar escolher resolução a cada Download.
21. Como usuário, quero ajustar o limite de qualidade em Configurações, para economizar espaço ou ter mais qualidade.
22. Como usuário, quero que todos os vídeos de um carrossel sejam baixados (ignorando fotos), para ter o Post completo.
23. Como usuário, quero ser avisado antes de começar se não houver espaço livre suficiente, para não ter Downloads falhando no meio.
24. Como usuário, quero que Downloads longos (ex.: lives de horas) sejam permitidos, para não ter limite artificial de duração.

### Falhas e Extratores

25. Como usuário, quero uma mensagem clara quando um Download falhar porque a Plataforma mudou, para entender que não é culpa minha.
26. Como usuário, quero um botão "Atualizar extratores e tentar de novo", para resolver a falha sem reinstalar o app.
27. Como usuário, quero que o app verifique atualizações de Extratores uma vez por semana, para continuar funcionando quando as Plataformas mudarem.
28. Como usuário, quero uma mensagem "Este Post exige login e não é suportado" para Posts privados, restritos por idade ou stories, para saber que não adianta tentar de novo.
29. Como usuário, quero que o app nunca tente de novo silenciosamente, para ter controle do que está acontecendo.

### Vídeos e arquivos

30. Como usuário, quero que cada Vídeo receba um nome padrão `{plataforma}_{autor}_{data}`, para identificar a origem pelo nome do arquivo.
31. Como usuário, quero que nomes repetidos recebam sufixo (`_2`, `_3`), para que Vídeos do mesmo autor no mesmo dia não se sobrescrevam.
32. Como usuário, quero renomear um Vídeo e ter o arquivo no celular renomeado junto, para que o nome seja o mesmo dentro e fora do app.
33. Como usuário, quero ser impedido de renomear para um nome que já existe na pasta, para não perder arquivos.
34. Como usuário, quero definir a Pasta de destino padrão em Configurações (padrão Movies/Baixaê), para escolher onde os vídeos ficam.
35. Como usuário, quero trocar a Pasta de destino num Download específico iniciado pela aba Baixar, para casos excepcionais.
36. Como usuário, quero ver de cada Vídeo a Plataforma, autor, legenda/título do Post, data do Download, duração e miniatura, para reconhecê-lo.
37. Como usuário, quero um botão "Abrir original" que abre o Link do Post, para voltar à fonte.
38. Como usuário, quero apagar um Vídeo com uma confirmação que inclui "Apagar também o arquivo do celular" (marcada por padrão), para escolher se o arquivo fica.
39. Como usuário, quero ver um Vídeo cujo arquivo sumiu marcado como Arquivo ausente, com opção de removê-lo da Biblioteca, para manter o app coerente com o celular.
40. Como usuário, quero gerar uma Versão sem som de um Vídeo, mantendo o original, para reutilizar só a imagem.

### Bibliotecas

41. Como usuário, quero que cada Vídeo vá automaticamente para a Biblioteca padrão da sua Plataforma, para ter organização sem esforço.
42. Como usuário, quero ver as Bibliotecas numa grade na aba Bibliotecas, para navegar pelo acervo.
43. Como usuário, quero criar Bibliotecas personalizadas (ex.: "Receitas"), para organizar por assunto.
44. Como usuário, quero renomear Bibliotecas personalizadas, para ajustar a organização.
45. Como usuário, quero que as Bibliotecas padrão não possam ser apagadas nem renomeadas, para que sempre exista um lugar para cada Plataforma.
46. Como usuário, quero mover um ou vários Vídeos (seleção por toque longo) para outra Biblioteca, para reorganizar rápido.
47. Como usuário, quero que mover um Vídeo entre Bibliotecas não mexa no arquivo do celular, para que a organização do app não bagunce meu armazenamento.
48. Como usuário, quero que, ao apagar uma Biblioteca personalizada, seus Vídeos voltem à Biblioteca padrão da Plataforma de origem, para não perder Vídeos sem querer.
49. Como usuário, quero filtrar Vídeos pela Plataforma de origem mesmo depois de movê-los, para saber de onde vieram.
50. Como usuário, quero ver os Vídeos de uma Biblioteca do Download mais recente para o mais antigo, para achar o que baixei por último.
51. Como usuário, quero buscar Vídeos por nome, autor e legenda, para encontrar algo específico.

### Compartilhamento e Marca d'água

52. Como usuário, quero compartilhar um Vídeo pelo menu Compartilhar padrão do Android, para enviá-lo a qualquer app (WhatsApp, Instagram etc.).
53. Como usuário, quero ligar ou desligar a Marca d'água em Configurações (desligada por padrão), para divulgar o app quando quiser.
54. Como usuário, quero que a Marca d'água ("Baixado com Baixaê", canto inferior direito, semitransparente) seja aplicada só na cópia compartilhada, para que o Vídeo da Biblioteca continue limpo.
55. Como usuário, quero ver o progresso enquanto a cópia com Marca d'água é gerada, já que isso pode levar algum tempo.

### App

56. Como usuário, quero navegar por três abas — Baixar, Bibliotecas, Configurações — para encontrar tudo rapidamente.
57. Como usuário, quero a interface em português, para usar o app no meu idioma.
58. Como usuário, quero ser avisado quando houver uma nova versão do app no GitHub, com link para o APK, para manter o app atualizado fora da Play Store.
59. Como usuário, quero baixar um APK específico para a arquitetura do meu celular, para instalar um arquivo menor.
60. Como usuário, quero que o app seja gratuito e de código aberto, para confiar no que ele faz.

## Implementation Decisions

- **Plataforma e stack:** Android nativo em Kotlin com Jetpack Compose; mínimo Android 8.0 (API 26). Textos em recursos de string pt-BR, prontos para tradução.
- **Núcleo independente do Android:** todas as regras de domínio vivem num módulo Kotlin puro (JVM), sem dependências Android. O app Android é uma casca fina que liga o núcleo a adaptadores reais e às telas.
- **Portas do núcleo** (interfaces implementadas por adaptadores):
  - **Extrator**: dado um Link, devolve os metadados do Post (autor, legenda, duração, miniatura, lista de vídeos) e baixa um vídeo respeitando um limite de qualidade, reportando progresso e permitindo cancelamento. Erros tipados: Plataforma mudou / exige login / sem rede / sem espaço / outro. Também expõe "atualizar Extratores" e a versão atual.
  - **Armazenamento de arquivos**: criar, renomear, apagar e verificar existência de arquivos numa Pasta de destino; espaço livre disponível.
  - **Repositório**: persistência de Vídeos, Bibliotecas e Fila de Downloads.
  - **Relógio** e **Conectividade** (online/offline, Wi-Fi ou não).
  - **Processador de vídeo**: remover áudio (sem recodificar) e aplicar Marca d'água (recodifica).
  - **Verificador de versões**: consulta a versão mais nova do app no GitHub Releases.
- **Reconhecimento de Plataforma** é lógica pura do núcleo, baseada no domínio do Link; Plataformas da v1: Instagram, Facebook (instável), X (twitter.com e x.com), Bluesky, YouTube. Qualquer outro domínio é recusado — o extrator genérico do yt-dlp não é usado.
- **Adaptador de Extrator v1:** youtubedl-android (yt-dlp + Python + ffmpeg + QuickJS para o YouTube), isolado atrás da porta Extrator para poder ser trocado (ex.: runtime do YTDLnis). yt-dlp atualizável em tempo de execução (canal estável).
- **Fila de Downloads:** máximo 2 Downloads simultâneos; executa em segundo plano com notificação de progresso (trabalho persistente do Android que sobrevive ao fechamento do app); estados: pendente, aguardando rede, em andamento, concluído, falhou, cancelado. Sem pausa. Sem nova tentativa automática após falha; espera de rede não conta como falha.
- **Modelo de domínio:**
  - Vídeo: id, nome (= nome do arquivo), Plataforma de origem, Link do Post, autor, legenda/título, data do Download, duração, miniatura, caminho do arquivo, Biblioteca (exatamente uma), estado de arquivo (presente / Arquivo ausente), origem opcional (para Versão sem som).
  - Biblioteca: id, nome, tipo (padrão com Plataforma associada | personalizada). Bibliotecas padrão criadas na primeira execução, uma por Plataforma; imutáveis.
  - Apagar Biblioteca personalizada move seus Vídeos para a Biblioteca padrão da Plataforma de origem de cada um.
- **Nomes de arquivo:** padrão `{plataforma}_{autor}_{data ISO}`; colisões na Pasta de destino recebem sufixo `_2`, `_3`…; renomear para nome existente é recusado; caracteres inválidos em nomes de arquivo são sanitizados.
- **Pasta de destino:** padrão Movies/Baixaê via armazenamento compartilhado do Android; configurável em Configurações; Downloads do menu Compartilhar sempre usam a padrão.
- **Link repetido:** detectado pelo Link do Post já existir em algum Vídeo.
- **Área de transferência:** lida apenas quando o app está em primeiro plano, e só para sugerir; nunca preenche nem baixa sem toque do usuário.
- **Marca d'água:** texto fixo no canto inferior direito, semitransparente, gerado numa cópia temporária usada só no Compartilhamento; o arquivo da Biblioteca nunca é alterado.
- **Versão sem som:** novo Vídeo na mesma Biblioteca, nome `<nome> (sem som)` com regra de sufixo, sem recodificar.
- **Verificações semanais:** Extratores e nova versão do app, numa mesma tarefa periódica.
- **Distribuição:** GPL-3.0 (exigência do youtubedl-android); APKs separados por ABI no GitHub Releases.

## Testing Decisions

- **Bons testes verificam comportamento externo**, não detalhes de implementação: entram pela API pública do núcleo e afirmam sobre o estado observável (Vídeos, Bibliotecas, Fila, arquivos no armazenamento falso), nunca sobre chamadas internas.
- **Ponto principal de teste (único seam):** a API pública do núcleo Kotlin, com fakes controláveis para Extrator, Armazenamento de arquivos, Repositório (em memória), Relógio, Conectividade, Processador de vídeo e Verificador de versões. Ex.: "dado um Link do Instagram com 3 vídeos, quando o Download termina, a Biblioteca padrão Instagram tem 3 Vídeos com sufixos `_2` e `_3`".
- **Módulos testados:** reconhecimento de Plataforma, Fila de Downloads (concorrência, rede, cancelamento, falhas), Bibliotecas (padrão, personalizadas, mover, apagar), nomes e colisões, Link repetido, Arquivo ausente, Versão sem som, decisão de Marca d'água, verificações semanais.
- **Contrato do Extrator real:** suíte manual (não roda no CI) que executa o adaptador youtubedl-android contra um Link público de cada Plataforma; útil após atualizar o yt-dlp.
- **Interface:** poucos testes de Compose cobrindo o fluxo principal de cada aba.
- **Prior art:** nenhuma (projeto novo); o primeiro ticket estabelece o padrão.

## Out of Scope

- Distribuição na Google Play.
- Threads (até o yt-dlp suportar; ADR-0004).
- Botão sobreposto ao app da Plataforma e serviço de acessibilidade (ADR-0003); bolha flutuante fica como ideia futura.
- Login nas Plataformas: stories, contas privadas, conteúdo restrito por idade.
- Download de fotos e extração só de áudio (MP3).
- Escolha de resolução a cada Download.
- Pausar e retomar Downloads.
- Texto e posição personalizáveis da Marca d'água.
- Exportar/importar Bibliotecas; importar vídeos já existentes na pasta.
- Idiomas além de pt-BR.
- Servidor próprio ou APIs pagas de extração.

## Further Notes

- youtubedl-android está sem releases desde nov/2025; monitorar e, se necessário, trocar o adaptador do Extrator.
- O YouTube muda com frequência (SABR, desafios JS); a verificação semanal de Extratores é essencial.
- Facebook tem alta taxa de falha em 2026 — por isso é sinalizado como Plataforma instável.
- APK com yt-dlp + Python + ffmpeg tem ~35–45 MB por ABI.
