# OpenVista — streams HLS de paisagens, praias e cidades (pesquisa local)

**Data do teste:** 25 de outubro de 2026. **Propósito:** avaliar reprodução técnica no PC e futuramente na Hisense, **não** distribuir câmeras junto ao OpenVista.

**Resultado:** 38 URLs únicos com vídeo identificado em **1920×1080 ou superior**, em 52 tentativas de ffprobe distribuídas em duas baterias (2 links repetidos entre as baterias). O ffprobe abriu a transmissão e informou codec/resolução; **isso não é um teste contínuo de reprodução nem um teste no Fire OS/Media3**.

Fontes dos candidatos: [Live-Environment-Streams](https://github.com/william-ricchiuti/Live-Environment-Streams) (dados públicos de pesquisa); resolução atual medida diretamente pelo **ffprobe no Windows**, diferente de apenas confiar nos metadados do repositório. Não houve autenticação, extração de token temporário, contorno de paywall ou download prolongado de vídeo.

## Playlist de testes

Abra [scenic-fullhd-test-only-2026-10-25.m3u](scenic-fullhd-test-only-2026-10-25.m3u) no VLC ou player compatível com HLS. **Arquivo de testes separado do código do app**. Não está no catálogo distribuído, nos assets do APK ou na configuração de reprodução padrão.

## Links aprovados apenas na verificação técnica de resolução

| Ambiente | Local | Resolução medida | Codec | Manifest HLS |
| --- | --- | ---: | --- | --- |
| Praias e litoral | Apúlia | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/bcapulia/playlist.m3u8` |
| Praias e litoral | Arrifana | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/arrifana/playlist.m3u8` |
| Praias e litoral | Carcavelos | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/carcavelos/playlist.m3u8` |
| Praias e litoral | Costa da Caparica | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/costacaparicacds/playlist.m3u8` |
| Praias e litoral | Costa da Caparica — Riviera | 2560×1440 | h264 | `https://video-auth1.iol.pt/beachcam/riviera/playlist.m3u8` |
| Praias e litoral | Dębki — praia | 2688×1520 | h264 | `https://ls.tkchopin.pl:9043/live/debki_plaza_static.stream/playlist.m3u8` |
| Praias e litoral | Espinho | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/espinho/playlist.m3u8` |
| Praias e litoral | Faro — Praia | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/bcfaro/playlist.m3u8` |
| Praias e litoral | Gdynia Orłowo — praia | 1920×1080 | h264 | `https://ls.tkchopin.pl:9043/live/gdynia_orlowo_plaza_obrotowa.stream/playlist.m3u8` |
| Praias e litoral | Hel — boulevard | 1920×1080 | h264 | `https://ls.tkchopin.pl:9043/live/hel_bulwar_nadmorski_obrotowa.stream/playlist.m3u8` |
| Praias e litoral | Jastarnia — praia | 2560×1440 | h264 | `https://ls.tkchopin.pl:9043/live/jastarnia_plaza_dom_zdrojowy_obrotowa.stream/playlist.m3u8` |
| Praias e litoral | Jastarnia — píer | 2560×1440 | h264 | `https://ls.tkchopin.pl:9043/live/jastarnia_molo_static.stream/playlist.m3u8` |
| Praias e litoral | Jurata — píer | 2560×1440 | h264 | `https://ls.tkchopin.pl:9043/live/jurata_molo_static.stream/playlist.m3u8` |
| Praias e litoral | Kalamata — praia (Grécia) | 2560×1440 | h264 | `https://livecameras.gr:30443/live/kala3/playlist.m3u8` |
| Praias e litoral | Leça da Palmeira | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/lecadapalmeira/playlist.m3u8` |
| Praias e litoral | Matosinhos | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/matosinhos/playlist.m3u8` |
| Praias e litoral | Meia Praia | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/meiapraia/playlist.m3u8` |
| Praias e litoral | Nazaré — Praia do Norte | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/nazareparadonorte/playlist.m3u8` |
| Praias e litoral | Sopot — Praia | 2560×1440 | h264 | `https://ls.tkchopin.pl:9043/live/sopot_plaza_male_molo_obrotowa.stream/playlist.m3u8` |
| Praias e litoral | Sopot — píer | 1920×1080 | h264 | `https://ls.tkchopin.pl:9043/live/sopot_molo_skwer_kuracyjny_obrotowa.stream/playlist.m3u8` |
| Praias e litoral | Władysławowo — praia | 1920×1080 | h264 | `https://ls.tkchopin.pl:9043/live/wladyslawowo_plaza_obrotowa.stream/playlist.m3u8` |
| Lagos e marinas | Gdynia — marina | 1920×1080 | h264 | `https://ls.tkchopin.pl:9043/live/gdynia_marina_obrotowa.stream/playlist.m3u8` |
| Lagos e marinas | Giżycko — marina do lago | 2688×1520 | h264 | `https://stream.mazury24.eu/m_lester2/index.m3u8` |
| Lagos e marinas | Kietlice — lago | 1920×1080 | h264 | `https://stream.mazury24.eu/m_kietlice/index.m3u8` |
| Lagos e marinas | Puck — píer | 1920×1080 | h264 | `https://ls.tkchopin.pl:9043/live/puck_molo_static.stream/playlist.m3u8` |
| Cidades e arquitetura | Arachova — cidade (Grécia) | 1920×1080 | h264 | `https://livecameras.gr:30443/live/arach1/playlist.m3u8` |
| Cidades e arquitetura | Cracóvia — praça principal | 1920×1080 | h264 | `https://hoktastream2.webcamera.pl/krakow_cam_702b61/krakow_cam_702b61.stream/playlist.m3u8` |
| Cidades e arquitetura | Florença — centro histórico | 1920×1080 | h264 | `https://5e0add8153fcd.streamlock.net:1936/vedetta/via-della-scala.stream/chunklist_w1359048502.m3u8` |
| Cidades e arquitetura | Malbork — castelo | 1920×1080 | h264 | `https://hoktastream2.webcamera.pl/malbork_cam_0ffc99/malbork_cam_0ffc99.stream/playlist.m3u8` |
| Cidades e arquitetura | Varsóvia — praça do castelo | 1920×1080 | h264 | `https://hoktastream2.webcamera.pl/warszawa_cam_5f3b1a/warszawa_cam_5f3b1a.stream/chunks.m3u8` |
| Cidades e arquitetura | Wrocław — praça da cidade | 1920×1080 | h264 | `https://hoktastream1.webcamera.pl/wroclaw_przed_cam_16c68e/wroclaw_przed_cam_16c68e.stream/playlist.m3u8` |
| Cidades e arquitetura | Zakopane — rua central | 1920×1080 | h264 | `https://hoktastream5.webcamera.pl/krupowki2_cam_62169a/krupowki2_cam_62169a.stream/playlist.m3u8` |
| Montanhas | Bryce Resort — montanhas | 1920×1080 | h264 | `https://stream.resortcams.com/live/bryceresort.stream/playlist.m3u8` |
| Montanhas | Bukowina — hotel | 1920×1080 | h264 | `https://hoktastream1.webcamera.pl/bukovina_cam_7aa04f/bukovina_cam_7aa04f.stream/playlist.m3u8` |
| Montanhas | Bukowina — panorama | 5120×1440 | h264 | `https://hoktastream1.webcamera.pl/skywind_cam_3c5be6/skywind_cam_3c5be6.stream/playlist.m3u8` |
| Montanhas | Karpacz — panorama | 1920×1080 | h264 | `https://hoktastream2.webcamera.pl/karpacz_cam_ab594f/karpacz_cam_ab594f.stream/playlist.m3u8` |
| Montanhas | Rzepiska — panorâmica das Tatras | 1920×1080 | h264 | `https://hoktastream1.webcamera.pl/airnet_cam_d60f3c/airnet_cam_d60f3c.stream/playlist.m3u8` |
| Montanhas | Wintergreen — montanhas | 1920×1080 | h264 | `https://stream.resortcams.com/live/wintergreen2.stream/playlist.m3u8` |

## Teste adicional: quadro decodificado

Além dos 52 testes de sondagem, o **FFmpeg conseguiu decodificar um quadro real de vídeo no Windows** em quatro transmissões: **Nazaré (1920×1080)**, **Kalamata (2560×1440)**, **Sopot (2560×1440)** e **Varsóvia (1920×1080)**, sem salvar imagens ou vídeos. Os resultados estão em [scenic-sample-frame-decode-2026-10-25.json](scenic-sample-frame-decode-2026-10-25.json). Isso comprova uma amostra, **não** estabilidade prolongada nem funcionamento no Media3 da TV.

## Observações técnicas

- **Full HD** foi definido como largura de pelo menos 1920 pixels **e** altura de pelo menos 1080. A câmera superwide 5120×1440 não deve ser confundida com 4K UHD 3840×2160.
- **Sem estabilidade garantida:** câmeras mudam de URL, restringem acessos, entram em manutenção, podem ficar escuras à noite ou trocar de qualidade.
- **Problemas encontrados:** algumas transmissões de Porto Rico e da República Dominicana não responderam ao ffprobe nesta rodada; suas URLs não foram incluídas na playlist Full HD.
- **Teste seguinte:** reproduzir cenas selecionadas por vários minutos no Media3 da Hisense com métricas de quedas de frames, buffer, CPU e RAM, comparando 1080p, 1440p e panorâmicas superwide.
- **Direitos:** estes são endereços para teste técnico privado, não uma declaração de autorização para colocar seus vídeos num APK publicado. Se o produto algum dia passar a distribuir um catálogo com feeds incorporados, será necessária revisão dos termos de cada operador.

Comandos para repetir a pesquisa: baixar `streams.geojson` do repositório-fonte para `%TEMP%\openvista-streams.geojson` e executar `python scripts/probe_scenic_streams.py`, depois `python scripts/probe_scenic_city_streams.py`, seguido de `python scripts/build_scenic_test_playlist.py`. Estes testes usam `ffprobe`, não alteram a TV.
