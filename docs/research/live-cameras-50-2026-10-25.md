# 50 câmeras ao vivo ao redor do mundo — pesquisa de 25/10/2026

**Status: documento de pesquisa. NÃO é o catálogo aprovado da v1 do OpenVista.**

Leia isto antes de usar qualquer link abaixo como fonte do app:

- **Nenhuma destas 50 câmeras conta, ainda, para a meta da v1** (25 câmeras autorizadas, familiares e testadas na Hisense 58U6HF / Fire OS 7). A regra do repositório continua valendo: só conta depois de (1) permissão/atribuição do operador, (2) stream HLS/DASH direto e jogável e (3) teste real no aparelho. Veja o modelo de evidência em [webcam-source-evidence-template.md](webcam-source-evidence-template.md) e o contrato da v1 em [../product/v1-acceptance.md](../product/v1-acceptance.md).
- **Os links abaixo são páginas oficiais de exibição, não streams diretos.** Não foi extraído, copiado ou publicado nenhum token, URL de manifesto com sessão ou link interno de player. Alguns operadores usam players com tokens temporários; extrair esses links para embutir no app seria contornar o controle do operador e, por isso, não foi feito aqui.
- **Os vídeos, marcas e imagens pertencem aos respectivos operadores.** Este documento só lista links públicos para visualização no navegador. Não redistribui vídeo, não afirma licença de uso no OpenVista e não altera os termos de cada operador — antes de qualquer integração no app, é preciso ler os termos do operador e, quando exigido, pedir autorização por escrito.
- **"Funcionando" aqui significa o que foi testado em 25/10/2026** (método abaixo). Câmera ao vivo pode sair do ar, ficar escura à noite no fuso local, entrar em manutenção ou trocar de URL a qualquer momento. Uma página que hoje responde 200 não é garantia de disponibilidade contínua.

## O que o repositório já era antes deste documento

O OpenVista foi criado em 24/10/2026 (23h19 PDT; 25/10/2026 06:19 UTC) e, até esta pesquisa, estava em fase de descoberta/definição do produto: README, 78 decisões confirmadas, plano de implementação e documentos de pesquisa — **sem APK, sem código do app e sem nenhuma câmera verificada**. Este documento não muda esse estado: ele acrescenta uma lista de candidatos pesquisados, não um app funcionando.

## Método de verificação — 25/10/2026, manhã (America/Los_Angeles)

1. Ponto de partida: a página de câmeras mais populares da SkylineWebcams em 25/10/2026 (179 câmeras listadas naquele dia) + páginas em destaque na home da EarthCam no mesmo dia + 3 páginas oficiais avulsas (Abbey Road Studios, NPS Yellowstone e Explore.org).
2. Critério de seleção: diversidade de país e categoria (cidade, praia, ponto turístico, natureza, vida selvagem, vulcão, porto), preferência por operadores antigos e com página própria estável, e no máximo uma repetição de local (Times Square aparece duas vezes, com dois operadores e ângulos diferentes: SkylineWebcams e EarthCam 4K).
3. Teste aplicado a cada um dos 50 links, por requisição HTTP com redirecionamento seguido: **resultado 50/50 com HTTP 200 e marcador de ao vivo na página** (título com "LIVE"/"Live Cam" ou player/vídeo na página). O teste verifica a página de exibição, não decodifica o vídeo quadro a quadro e não foi feito na TV Hisense.
4. Limite honesto do teste: ele foi feito a partir desta máquina virtual (IP de datacenter). Um operador pode responder de forma diferente em rede residencial, em outro país ou com bloqueio geográfico. Falha futura em um link não prova que a câmera morreu — e sucesso hoje não prova que ela ficará no ar.

## Resumo da lista

- **Total: 50 câmeras, 28 países.**
- **Por operador/fonte:** SkylineWebcams 38, EarthCam 9, Abbey Road Studios 1, National Park Service / Yellowstone Forever 1, Explore.org 1.
- **Por categoria (agrupada): cidade 16, praia 14, ponto turístico 9, natureza / vida selvagem / vulcão / porto / ilha 11.
- **Qualidade:** as páginas são apresentadas pelos operadores como HD, e a EarthCam destaca a Times Square em 4K. A resolução efetiva no navegador/TV depende da fonte, da conexão e do dispositivo — não foi medida bitrate por bitrate aqui, e nenhuma câmera é prometida como "4K garantida" neste documento.

## As 50 câmeras

| # | Câmera | País | Categoria | Fonte | Link |
| --- | --- | --- | --- | --- | --- |
| 1 | Times Square | Estados Unidos | Cidade | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/united-states/new-york/new-york/times-square.html](https://www.skylinewebcams.com/en/webcam/united-states/new-york/new-york/times-square.html) |
| 2 | Empire State Building | Estados Unidos | Cidade | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/united-states/new-york/new-york/empire-state-building.html](https://www.skylinewebcams.com/en/webcam/united-states/new-york/new-york/empire-state-building.html) |
| 3 | Las Vegas Strip | Estados Unidos | Cidade | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/united-states/nevada/las-vegas/las-vegas.html](https://www.skylinewebcams.com/en/webcam/united-states/nevada/las-vegas/las-vegas.html) |
| 4 | Sydney | Austrália | Cidade/porto | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/australia/new-south-wales/sydney/sydney.html](https://www.skylinewebcams.com/en/webcam/australia/new-south-wales/sydney/sydney.html) |
| 5 | Shanghai | China | Cidade | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/china/shanghai/shanghai/skyline-of-shanghai.html](https://www.skylinewebcams.com/en/webcam/china/shanghai/shanghai/skyline-of-shanghai.html) |
| 6 | Torre Eiffel, Paris | França | Ponto turístico | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/france/ile-de-france/paris/paris-tour-eiffel.html](https://www.skylinewebcams.com/en/webcam/france/ile-de-france/paris/paris-tour-eiffel.html) |
| 7 | Fonte de Trevi, Roma | Itália | Ponto turístico | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/italia/lazio/roma/fontana-di-trevi.html](https://www.skylinewebcams.com/en/webcam/italia/lazio/roma/fontana-di-trevi.html) |
| 8 | Coliseu, Roma | Itália | Ponto turístico | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/italia/lazio/roma/roma-colosseo.html](https://www.skylinewebcams.com/en/webcam/italia/lazio/roma/roma-colosseo.html) |
| 9 | Praça de São Marcos, Veneza | Itália | Ponto turístico | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/italia/veneto/venezia/piazza-san-marco.html](https://www.skylinewebcams.com/en/webcam/italia/veneto/venezia/piazza-san-marco.html) |
| 10 | Duomo de Milão | Itália | Ponto turístico | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/italia/lombardia/milano/duomo-milano.html](https://www.skylinewebcams.com/en/webcam/italia/lombardia/milano/duomo-milano.html) |
| 11 | Ponte Vecchio, Florença | Itália | Ponto turístico | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/italia/toscana/firenze/ponte-vecchio.html](https://www.skylinewebcams.com/en/webcam/italia/toscana/firenze/ponte-vecchio.html) |
| 12 | Vulcão Stromboli | Itália | Vulcão | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/italia/sicilia/messina/stromboli.html](https://www.skylinewebcams.com/en/webcam/italia/sicilia/messina/stromboli.html) |
| 13 | Praga | Tchéquia | Cidade | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/czech-republic/prague/prague/prague.html](https://www.skylinewebcams.com/en/webcam/czech-republic/prague/prague/prague.html) |
| 14 | Plaza del Callao, Madri | Espanha | Cidade | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/espana/comunidad-de-madrid/madrid/madrid-plaza-del-callao.html](https://www.skylinewebcams.com/en/webcam/espana/comunidad-de-madrid/madrid/madrid-plaza-del-callao.html) |
| 15 | Los Cristianos, Tenerife | Espanha | Praia | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/espana/canarias/santa-cruz-de-tenerife/playa-los-cristianos.html](https://www.skylinewebcams.com/en/webcam/espana/canarias/santa-cruz-de-tenerife/playa-los-cristianos.html) |
| 16 | Las Vistas, Tenerife | Espanha | Praia | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/espana/canarias/santa-cruz-de-tenerife/playa-las-vistas.html](https://www.skylinewebcams.com/en/webcam/espana/canarias/santa-cruz-de-tenerife/playa-las-vistas.html) |
| 17 | Playa del Sardinero, Santander | Espanha | Praia | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/espana/cantabria/santander/playa-del-sardinero.html](https://www.skylinewebcams.com/en/webcam/espana/cantabria/santander/playa-del-sardinero.html) |
| 18 | Porto Seguro, Bahia | Brasil | Praia | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/brasil/bahia/porto-seguro/praia-de-taperapuan.html](https://www.skylinewebcams.com/en/webcam/brasil/bahia/porto-seguro/praia-de-taperapuan.html) |
| 19 | Copacabana, Rio de Janeiro | Brasil | Praia | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/brasil/rio-de-janeiro/rio-de-janeiro/copacabana-beach.html](https://www.skylinewebcams.com/en/webcam/brasil/rio-de-janeiro/rio-de-janeiro/copacabana-beach.html) |
| 20 | Muro das Lamentações, Jerusalém | Israel | Ponto turístico | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/israel/jerusalem-district/jerusalem/western-wall.html](https://www.skylinewebcams.com/en/webcam/israel/jerusalem-district/jerusalem/western-wall.html) |
| 21 | Petra, Tesouro | Jordânia | Ponto turístico | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/jordan/maan/amman/petra-the-treasury.html](https://www.skylinewebcams.com/en/webcam/jordan/maan/amman/petra-the-treasury.html) |
| 22 | Parlamento Helênico, Atenas | Grécia | Cidade | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/ellada/atiki/athina/hellenic-parliament.html](https://www.skylinewebcams.com/en/webcam/ellada/atiki/athina/hellenic-parliament.html) |
| 23 | Santorini, Firostefani | Grécia | Ilha | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/ellada/naigaio/kyklades/santorini-firostefani.html](https://www.skylinewebcams.com/en/webcam/ellada/naigaio/kyklades/santorini-firostefani.html) |
| 24 | Jökulsárlón | Islândia | Natureza/glaciar | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/iceland/austurland/hofn/jokulsarlon.html](https://www.skylinewebcams.com/en/webcam/iceland/austurland/hofn/jokulsarlon.html) |
| 25 | Henningsvær, Ilhas Lofoten | Noruega | Natureza/aurora | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/norge/nordland/lofoten/henningsvaer.html](https://www.skylinewebcams.com/en/webcam/norge/nordland/lofoten/henningsvaer.html) |
| 26 | Tsavo East | Quênia | Vida selvagem | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/kenya/taita-taveta-county/voi/tsavo-east-national-park.html](https://www.skylinewebcams.com/en/webcam/kenya/taita-taveta-county/voi/tsavo-east-national-park.html) |
| 27 | Lower Zambezi | Zâmbia | Vida selvagem | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/zambia/lusaka-province/mafuta/zambezi-national-park.html](https://www.skylinewebcams.com/en/webcam/zambia/lusaka-province/mafuta/zambezi-national-park.html) |
| 28 | Willard Beach, Ballito | África do Sul | Praia | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/south-africa/kwazulu-natal/kwadukuza/willard-beach.html](https://www.skylinewebcams.com/en/webcam/south-africa/kwazulu-natal/kwadukuza/willard-beach.html) |
| 29 | Kuredu | Maldivas | Praia | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/maldives/lhaviyani-atoll/kurendhoo/maldives-kuredu.html](https://www.skylinewebcams.com/en/webcam/maldives/lhaviyani-atoll/kurendhoo/maldives-kuredu.html) |
| 30 | Anse Parnel, Takamaka | Seychelles | Praia | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/seychelles/mahe/takamaka/seychelles-takamaka.html](https://www.skylinewebcams.com/en/webcam/seychelles/mahe/takamaka/seychelles-takamaka.html) |
| 31 | Nungwi, Zanzibar | Tanzânia | Praia | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/zanzibar/zanzibar-north/nungwi/zanzibar-nungwi.html](https://www.skylinewebcams.com/en/webcam/zanzibar/zanzibar-north/nungwi/zanzibar-nungwi.html) |
| 32 | Cabo San Lucas | México | Praia | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/mexico/baja-california-sur/cabo-san-lucas/cabo-san-lucas.html](https://www.skylinewebcams.com/en/webcam/mexico/baja-california-sur/cabo-san-lucas/cabo-san-lucas.html) |
| 33 | Ushuaia | Argentina | Porto | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/argentina/tierra-del-fuego/ushuaia/ushuaia.html](https://www.skylinewebcams.com/en/webcam/argentina/tierra-del-fuego/ushuaia/ushuaia.html) |
| 34 | Plaza Mayor, Cusco | Peru | Cidade | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/peru/cusco/cusco/plaza-mayor.html](https://www.skylinewebcams.com/en/webcam/peru/cusco/cusco/plaza-mayor.html) |
| 35 | Blackpool | Reino Unido | Cidade/píer | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/united-kingdom/england/blackpool/blackpool.html](https://www.skylinewebcams.com/en/webcam/united-kingdom/england/blackpool/blackpool.html) |
| 36 | Lagos | Portugal | Praia | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/portugal/algarve/lagos/lagos-portugal.html](https://www.skylinewebcams.com/en/webcam/portugal/algarve/lagos/lagos-portugal.html) |
| 37 | Shibuya, Tóquio | Japão | Cidade | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/japan/kanto/tokyo/tokyo-shibuya.html](https://www.skylinewebcams.com/en/webcam/japan/kanto/tokyo/tokyo-shibuya.html) |
| 38 | Torres del Paine, Patagônia | Chile | Natureza | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/chile/magallanes-region/torres-del-paine/patagonia.html](https://www.skylinewebcams.com/en/webcam/chile/magallanes-region/torres-del-paine/patagonia.html) |
| 39 | Times Square 4K | Estados Unidos | Cidade | EarthCam | [https://www.earthcam.com/usa/newyork/timessquare/?cam=tsnorth4k](https://www.earthcam.com/usa/newyork/timessquare/?cam=tsnorth4k) |
| 40 | Waikoloa Village, Havaí | Estados Unidos | Praia | EarthCam | [https://www.earthcam.com/usa/hawaii/bigisland/waikoloa/](https://www.earthcam.com/usa/hawaii/bigisland/waikoloa/) |
| 41 | CN Tower, Toronto | Canadá | Cidade | EarthCam | [https://www.earthcam.com/world/canada/toronto/cntower/?cam=cntower1](https://www.earthcam.com/world/canada/toronto/cntower/?cam=cntower1) |
| 42 | Maho Beach, Sint Maarten | Sint Maarten | Praia/aeroporto | EarthCam | [https://www.earthcam.com/world/sintmaarten/](https://www.earthcam.com/world/sintmaarten/) |
| 43 | Skydeck, Chicago | Estados Unidos | Cidade | EarthCam | [https://www.earthcam.com/usa/illinois/chicago/skydeck/?cam=chicagoskydeck](https://www.earthcam.com/usa/illinois/chicago/skydeck/?cam=chicagoskydeck) |
| 44 | Pittsburgh | Estados Unidos | Cidade | EarthCam | [https://www.earthcam.com/usa/pennsylvania/pittsburgh/](https://www.earthcam.com/usa/pennsylvania/pittsburgh/) |
| 45 | Tamariu | Espanha | Praia | EarthCam | [https://www.earthcam.com/world/spain/tamariu/](https://www.earthcam.com/world/spain/tamariu/) |
| 46 | Porto de Los Angeles | Estados Unidos | Porto | EarthCam | [https://www.earthcam.com/usa/california/losangeles/port/?cam=portofla](https://www.earthcam.com/usa/california/losangeles/port/?cam=portofla) |
| 47 | Abbey Road Crossing, Londres | Reino Unido | Ponto turístico | Abbey Road Studios (oficial) | [https://www.abbeyroad.com/crossing](https://www.abbeyroad.com/crossing) |
| 48 | Old Faithful, Yellowstone | Estados Unidos | Natureza/gêiser | NPS / Yellowstone Forever | [https://www.nps.gov/yell/learn/photosmultimedia/webcams.htm](https://www.nps.gov/yell/learn/photosmultimedia/webcams.htm) |
| 49 | African River Wildlife Cam (Mpala) | Quênia | Vida selvagem | Explore.org | [https://explore.org/livecams/african-wildlife/african-river-wildlife-cam](https://explore.org/livecams/african-wildlife/african-river-wildlife-cam) |
| 50 | Nashville | Estados Unidos | Cidade | EarthCam | [https://www.earthcam.com/usa/tennessee/nashville/](https://www.earthcam.com/usa/tennessee/nashville/) |

## Observações por fonte

- **SkylineWebcams (38):** agregador antigo, com página própria por câmera. É a fonte mais homogênea da lista — bom para navegação, mas concentrar a futura integração em um único agregador é um risco: se os termos dele mudarem, muitas câmeras caem juntas. Tratar cada câmera como um operador distinto é errado; o interlocutor para permissão, na maioria dos casos, é a SkylineWebcams e/ou o dono da câmera original.
- **EarthCam (9):** rede própria desde 1996, com várias câmeras em 4K (Times Square é o carro-chefe). Páginas em destaque na home em 25/10/2026.
- **Abbey Road Crossing (1):** página oficial do Abbey Road Studios, com a famosa faixa de pedestres de Londres.
- **Old Faithful, Yellowstone (1):** página oficial do National Park Service que reúne as webcams do parque, incluindo o livestream do Old Faithful. As demais webcams do parque citadas na página são imagens estáticas — não confunda com o livestream.
- **African River Wildlife Cam (1):** Explore.org. Vida selvagem é, por natureza, imprevisível: a câmera pode estar ao vivo e, mesmo assim, não mostrar animal nenhum por longos períodos. Isso é normal e não é defeito.

## O que falta para qualquer uma destas virar fonte da v1

1. Identificar o dono/operador exato de cada câmera e a cláusula de permissão (ou pedir autorização por escrito).
2. Confirmar que existe um stream HLS (`.m3u8`) ou DASH (`.mpd`) público, estável e sem token de sessão, liberado para o uso pretendido.
3. Testar de fato na Hisense 58U6HF / Fire OS 7 com Media3: primeiro quadro, resolução, codec, 5–10 minutos sem travar, comportamento quando a câmera cai.
4. Registrar tudo no modelo de evidência, um registro por câmera, com data, decisão (APROVADA / REJEITADA / PENDENTE) e motivo.
5. Só então contar para a meta de 25 da v1 — e, mesmo assim, manter esta lista de pesquisa separada do catálogo aprovado, para não misturar "link que abriu no navegador" com "fonte aprovada para o app".

## Como revalidar esta lista depois

Repetir o teste de HTTP 200 + marcador de ao vivo nas 50 URLs e anotar a nova data. Links que passarem de 200 para 404, redirecionamento para página genérica ou página sem player devem sair da lista ou ir para uma seção de "fora do ar em <data>", nunca permanecer como se estivessem funcionando. Câmeras noturnas no fuso local devem ser rechecadas em horário diurno local antes de declarar falha.

---

*Verificação: 25/10/2026, manhã, America/Los_Angeles. Método: requisição HTTP com redirecionamento seguido, 50/50 com HTTP 200 e marcador de ao vivo. Não houve teste de reprodução na TV, não houve extração de stream interno e não há, neste documento, nenhuma afirmação de licença de uso no OpenVista.*
