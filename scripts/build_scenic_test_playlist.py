"""Build research-only scenic test playlist from real local ffprobe JSON results.

Does not change the OpenVista runtime catalog or authorize redistribution.
"""
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/"docs/research"
PROBES=[BASE/"scenic-1080p-probe-results-2026-10-25.json",BASE/"scenic-city-1080p-probe-results-2026-10-25.json"]
OUT_MD=BASE/"scenic-fullhd-test-links-2026-10-25.md"
OUT_M3U=BASE/"scenic-fullhd-test-only-2026-10-25.m3u"
data=[json.loads(p.read_text(encoding="utf-8")) for p in PROBES]
attempts=sum(d.get("attempted",0) for d in data)
merged={}
duplicates=0
for d in data:
    for r in d["results"]:
        if r.get("status") != "verified_1080plus":
            continue
        if r["url"] in merged:
            duplicates+=1
        else:
            merged[r["url"]]=r
rows=list(merged.values())
def group(r):
    s=(r["label"]+" "+r.get("name","")).lower()
    if any(x in s for x in ["praia","beach","nazaré","arrifana","carcavelos","caparica","faro","sopot","meia praia","matosinhos","dębki","jurata","jastarnia","hel","władysławowo","apúlia","espinho","leça","orłowo","kalamata"]):
        return "Praias e litoral"
    if any(x in s for x in ["lago","marina","píer","puck","giżycko","kietlice"]):
        return "Lagos e marinas"
    if any(x in s for x in ["montanha","tatras","bukowina","wintergreen","bryce","karpacz","rzepiska"]):
        return "Montanhas"
    return "Cidades e arquitetura"
sorts={"Praias e litoral":0,"Lagos e marinas":1,"Cidades e arquitetura":2,"Montanhas":3}
rows.sort(key=lambda r:(sorts[group(r)],r["label"].casefold()))
lines=[
"# OpenVista — streams HLS de paisagens, praias e cidades (pesquisa local)",
"",
"**Data do teste:** 25 de outubro de 2026. **Propósito:** avaliar reprodução técnica no PC e futuramente na Hisense, **não** distribuir câmeras junto ao OpenVista.",
"",
f"**Resultado:** {len(rows)} URLs únicos com vídeo identificado em **1920×1080 ou superior**, em {attempts} tentativas de ffprobe distribuídas em duas baterias ({duplicates} links repetidos entre as baterias). O ffprobe abriu a transmissão e informou codec/resolução; **isso não é um teste contínuo de reprodução nem um teste no Fire OS/Media3**.",
"",
"Fontes dos candidatos: [Live-Environment-Streams](https://github.com/william-ricchiuti/Live-Environment-Streams) (dados públicos de pesquisa); resolução atual medida diretamente pelo **ffprobe no Windows**, diferente de apenas confiar nos metadados do repositório. Não houve autenticação, extração de token temporário, contorno de paywall ou download prolongado de vídeo.",
"",
"## Playlist de testes",
"",
"Abra [scenic-fullhd-test-only-2026-10-25.m3u](scenic-fullhd-test-only-2026-10-25.m3u) no VLC ou player compatível com HLS. **Arquivo de testes separado do código do app**. Não está no catálogo distribuído, nos assets do APK ou na configuração de reprodução padrão.",
"",
"## Links aprovados apenas na verificação técnica de resolução",
"",
"| Ambiente | Local | Resolução medida | Codec | Manifest HLS |",
"| --- | --- | ---: | --- | --- |",
]
for x in rows:
    lines.append(f"| {group(x)} | {x['label']} | {x['actualWidth']}×{x['actualHeight']} | {x.get('codec') or 'desconhecido'} | `{x['url']}` |")
lines += [
"",
"## Teste adicional: quadro decodificado",
"",
"Além dos 52 testes de sondagem, o **FFmpeg conseguiu decodificar um quadro real de vídeo no Windows** em quatro transmissões: **Nazaré (1920×1080)**, **Kalamata (2560×1440)**, **Sopot (2560×1440)** e **Varsóvia (1920×1080)**, sem salvar imagens ou vídeos. Os resultados estão em [scenic-sample-frame-decode-2026-10-25.json](scenic-sample-frame-decode-2026-10-25.json). Isso comprova uma amostra, **não** estabilidade prolongada nem funcionamento no Media3 da TV.",
"",
"## Observações técnicas",
"",
"- **Full HD** foi definido como largura de pelo menos 1920 pixels **e** altura de pelo menos 1080. A câmera superwide 5120×1440 não deve ser confundida com 4K UHD 3840×2160.",
"- **Sem estabilidade garantida:** câmeras mudam de URL, restringem acessos, entram em manutenção, podem ficar escuras à noite ou trocar de qualidade.",
"- **Problemas encontrados:** algumas transmissões de Porto Rico e da República Dominicana não responderam ao ffprobe nesta rodada; suas URLs não foram incluídas na playlist Full HD.",
"- **Teste seguinte:** reproduzir cenas selecionadas por vários minutos no Media3 da Hisense com métricas de quedas de frames, buffer, CPU e RAM, comparando 1080p, 1440p e panorâmicas superwide.",
"- **Direitos:** estes são endereços para teste técnico privado, não uma declaração de autorização para colocar seus vídeos num APK publicado. Se o produto algum dia passar a distribuir um catálogo com feeds incorporados, será necessária revisão dos termos de cada operador.",
"",
"Comandos para repetir a pesquisa: baixar `streams.geojson` do repositório-fonte para `%TEMP%\\openvista-streams.geojson` e executar `python scripts/probe_scenic_streams.py`, depois `python scripts/probe_scenic_city_streams.py`, seguido de `python scripts/build_scenic_test_playlist.py`. Estes testes usam `ffprobe`, não alteram a TV.",
"",
]
OUT_MD.write_text("\n".join(lines).rstrip()+"\n",encoding="utf-8")
m3u=["#EXTM3U","# Test-only OpenVista playlist. Not approved for redistribution inside an app."]
for x in rows:
    name=x["label"].replace(","," ")
    m3u.append(f'#EXTINF:-1 group-title="{group(x)}", {name} ({x["actualWidth"]}x{x["actualHeight"]})')
    m3u.append(x["url"])
OUT_M3U.write_text("\n".join(m3u)+"\n",encoding="utf-8")
print(f"Generated {len(rows)} unique ffprobe-verified Full-HD+ test links; {attempts} attempts, {duplicates} duplicates; output: {OUT_MD.name} and {OUT_M3U.name}")
