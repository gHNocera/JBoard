# JBoard - Soundboard Java

O **JBoard** é um soundboard simples feito em Java, inspirado no SoundPad, que permite tocar arquivos de áudio (WAV/MP3) em diferentes dispositivos de saída, como fones de ouvido, caixas de som ou mixadores virtuais (ex: para uso no Discord).

## Funcionalidades

- Seleção do dispositivo de saída de áudio (ex: fone, alto-falante, mixer virtual)
- Lista de sons carregados automaticamente de uma pasta padrão (`audios`)
- Adição manual de arquivos de áudio à lista
- Controle de volume integrado
- Tecla de atalho configurável para tocar/pausar o áudio selecionado
- Botão para parar a reprodução
- Compatível com arquivos `.wav` e `.mp3`

## Como usar

1. Coloque seus arquivos de áudio na pasta `audios` (crie se não existir).
2. Execute o programa.
3. Selecione o dispositivo de saída desejado.
4. Selecione um áudio da lista e use os botões ou configure uma tecla de atalho para tocar/pausar.
5. Ajuste o volume conforme necessário.

## Observações

- Para uso em Discord, recomenda-se configurar o mixer virtual e converter os áudios para mono, 16 bits, 48000 Hz para melhor qualidade.
- O controle de volume depende do suporte do dispositivo selecionado.