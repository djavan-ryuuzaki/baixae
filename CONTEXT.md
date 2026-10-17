# Baixaê

App Android para baixar vídeos públicos de Plataformas sociais a partir de um Link e organizá-los em Bibliotecas dentro do app.

## Language

### Origem

**Plataforma**:
Serviço social suportado, de onde Vídeos são baixados: Instagram, Facebook, X, Bluesky e YouTube.
_Avoid_: rede, rede social, site

**Plataforma instável**:
Plataforma suportada cujos Downloads falham com frequência; é sinalizada como tal ao usuário (hoje: Facebook).

**X**:
A Plataforma antes chamada Twitter; Links de twitter.com e x.com pertencem a ela.
_Avoid_: Twitter

**Post**:
Uma publicação numa Plataforma, que contém um ou mais vídeos. Existe na Plataforma, não no app.
_Avoid_: publicação, conteúdo

**Link**:
Endereço de um Post, colado ou recebido pelo menu Compartilhar, a partir do qual um Download começa. Links de fora das Plataformas são recusados.
_Avoid_: URL, endereço

**Download**:
O ato de obter os vídeos de um Post a partir do seu Link e salvá-los no celular, gerando um ou mais Vídeos.
_Avoid_: baixada, captura

**Fila de Downloads**:
Sequência de Downloads pendentes ou em andamento, que continua mesmo com o app fechado.

**Extrator**:
O componente que sabe obter vídeos de uma Plataforma; o usuário pode atualizá-lo quando uma Plataforma muda e os Downloads começam a falhar.
_Avoid_: motor, scraper

### Acervo

**Vídeo**:
Item baixado e registrado no app, ligado a um arquivo no celular, com Plataforma de origem fixa e o Link do Post de onde veio. Seu nome é o próprio nome do arquivo.
_Avoid_: mídia, item, arquivo

**Biblioteca**:
Agrupamento de Vídeos que existe só dentro do app; cada Vídeo pertence a exatamente uma Biblioteca. Não corresponde a uma pasta no celular.
_Avoid_: pasta, álbum, coleção, etiqueta

**Biblioteca padrão**:
Biblioteca que existe para cada Plataforma e recebe automaticamente os Vídeos baixados dela. Não pode ser apagada nem renomeada.

**Biblioteca personalizada**:
Biblioteca criada pelo usuário. Ao ser apagada, seus Vídeos voltam à Biblioteca padrão da sua Plataforma de origem.
_Avoid_: Biblioteca do usuário

**Pasta de destino**:
Local no armazenamento do celular onde o arquivo de um Vídeo é salvo. Há uma padrão definida em Configurações, que pode ser trocada em cada Download.
_Avoid_: diretório, Biblioteca

**Arquivo ausente**:
Estado de um Vídeo cujo arquivo foi apagado ou movido fora do app.

**Versão sem som**:
Novo Vídeo derivado de outro, sem o áudio original, criado na mesma Biblioteca; o Vídeo original não é alterado.
_Avoid_: vídeo mudo

### Saída

**Compartilhamento**:
Envio de um Vídeo do app para outro app. Quando a Marca d'água está ligada, envia uma cópia marcada, nunca o arquivo da Biblioteca.

**Marca d'água**:
Texto visível "Baixado com Baixaê" num canto do vídeo, aplicado apenas no Compartilhamento. Desligada por padrão.
