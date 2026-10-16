# 05: Receber Links pelo menu Compartilhar

**What to build:** o app aparece como destino no menu Compartilhar do Android. Ao compartilhar um Post de uma Plataforma para o app, aparece só um aviso "Adicionado à fila" e o usuário continua no app de origem; o Download segue na Fila de Downloads usando a Pasta de destino padrão. Ver ADR-0003.

**Blocked by:** 04

**Status:** done

- [x] Compartilhar um Post do Instagram/YouTube/X/Bluesky/Facebook mostra o app como opção
- [x] Escolher o app adiciona o Download à fila e mostra "Adicionado à fila" sem abrir a interface completa
- [x] O usuário permanece no app de origem
- [x] Texto compartilhado que contém um Link no meio de outras palavras é aceito (Link extraído)
- [x] Link não suportado compartilhado mostra aviso de recusa, sem enfileirar

## Comments

- `CompartilharActivity` transparente (sem interface) recebe `ACTION_SEND text/plain`, extrai o Link com `Plataforma.extrairLink`, enfileira na Pasta de destino padrão e mostra um Toast. Verificado no emulador simulando o compartilhamento a partir do Chrome (o Chrome continuou em primeiro plano) e com um Link do TikTok (recusado, nada enfileirado). O compartilhamento real a partir dos apps de cada Plataforma não foi testado.
