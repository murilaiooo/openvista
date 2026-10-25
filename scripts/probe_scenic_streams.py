"""Limited local ffprobe verification of scenic HLS URLs from public research dataset.

For feasibility tests only: no embedding, redistribution, recording or bulk monitoring.
Never bypass tokens, paywalls, logins, regional restrictions or anti-bot controls.
"""
import concurrent.futures
import json
from pathlib import Path
import subprocess
import threading
import sys
sys.stdout.reconfigure(encoding='utf-8', errors='replace')
from urllib.parse import urlsplit

ROOT=Path(__file__).resolve().parents[1]
SOURCE=Path.home()/"AppData/Local/Temp/openvista-streams.geojson"
OUT=ROOT/"docs/research/scenic-1080p-probe-results-2026-10-25.json"
TARGETS=[
("Nazaré — Praia do Norte","nazareparadonorte"),
("Arrifana","/arrifana/"),
("Carcavelos","/carcavelos/"),
("Faro — Beach","/bcfaro/"),
("Apúlia","/bcapulia/"),
("Matosinhos","/matosinhos/"),
("Meia Praia","/meiapraia/"),
("Espinho","/espinho/"),
("Costa da Caparica","/costacaparicacds/"),
("Leça da Palmeira","/lecadapalmeira/"),
("Costa da Caparica — Riviera","/riviera/"),
("Sopot — Beach","sopot_plaza_male_molo_obrotowa"),
("Sopot — Pier","sopot_molo_skwer_kuracyjny_obrotowa"),
("Dębki — Beach","debki_plaza_static"),
("Jastarnia — Beach","jastarnia_plaza_dom_zdrojowy_obrotowa"),
("Jastarnia — Pier","jastarnia_molo_static"),
("Jurata — Pier","jurata_molo_static"),
("Gdynia Orłowo — Beach","gdynia_orlowo_plaza_obrotowa"),
("Gdynia — Marina","gdynia_marina_obrotowa"),
("Hel — boulevard","hel_bulwar_nadmorski_obrotowa"),
("Władysławowo — Beach","wladyslawowo_plaza_obrotowa"),
("Puck — Pier","puck_molo_static"),
("Kalamata — Beach (Greece)","/live/kala3/"),
("Arachova — Town (Greece)","/live/arach1/"),
("Giżycko — Lake Marina","/m_lester2/"),
("Mikołajki — Lake","/m_mila1/"),
("Kietlice — Lake","/m_kietlice/"),
("Isla Verde — Beach (Puerto Rico)","s62.ipcamlive.com"),
("Wyndham Rio Mar — Beach (Puerto Rico)","s116.ipcamlive.com"),
("Sunbay Marina (Porto Rico)","s90.ipcamlive.com"),
("Puerto Del Rey Marina (Porto Rico)","s112.ipcamlive.com"),
("Cabarete — Beach (Dominican Republic)","s141.ipcamlive.com"),
("Pinamar — Coast (Argentina)","wowza.telpin.com.ar"),
("Rzepiska — Tatra Panorama","airnet_cam_d60f3c"),
("Bukowina — panorama","skywind_cam_3c5be6"),
]
features=json.loads(SOURCE.read_text(encoding="utf-8"))["features"]
props=[f["properties"] for f in features]
choices=[]
for label,needle in TARGETS:
    xs=[p for p in props if needle.lower() in (p.get("url") or "").lower()]
    if len(xs)!=1:
        print("SKIP ambiguous or not found",label,len(xs),flush=True)
        continue
    p=xs[0]
    url=p["url"]
    if p.get("url_type")!="hls" or "token=" in url.lower() or p.get("source_url_requires"):
        print("SKIP requires token or non-direct",label,flush=True)
        continue
    choices.append({"label":label,"name":p.get("name"),"source":p.get("source_family"),"url":url,"reportedResolution":p.get("resolution"),"reportedLastVerified":p.get("last_verified")})
domain_limits={}
domain_lock=threading.Lock()
def probe(item):
    host=urlsplit(item["url"]).netloc
    with domain_lock:
        domain_limits.setdefault(host,threading.BoundedSemaphore(2))
        limiter=domain_limits[host]
    with limiter:
        cmd=["ffprobe","-v","error","-rw_timeout","9000000","-probesize","500000","-analyzeduration","1000000","-select_streams","v:0","-show_entries","stream=codec_name,width,height","-of","json",item["url"]]
        try:
            p=subprocess.run(cmd,capture_output=True,timeout=17,encoding="utf-8",errors="replace")
            if p.returncode!=0:
                return {**item,"status":"failed","reason":p.stderr.strip()[-210:]}
            d=json.loads(p.stdout)
            streams=d.get("streams",[])
            stream=next((s for s in streams if int(s.get("width",0))>0),None)
            if not stream:
                return {**item,"status":"no_video","reason":"no video stream in ffprobe output"}
            width=int(stream.get("width",0));height=int(stream.get("height",0))
            return {**item,"status":"verified_1080plus" if width>=1920 and height>=1080 else "verified_below_fullhd","actualWidth":width,"actualHeight":height,"codec":stream.get("codec_name")}
        except subprocess.TimeoutExpired:
            return {**item,"status":"timeout","reason":"ffprobe >17 seconds"}
        except Exception as e:
            return {**item,"status":"error","reason":str(e)[:180]}
print("Testing",len(choices),"distinct 1080+ candidate streams; 5 global probes max, 2 per host.",flush=True)
results=[]
with concurrent.futures.ThreadPoolExecutor(max_workers=5) as ex:
    fut={ex.submit(probe,c):c for c in choices}
    for f in concurrent.futures.as_completed(fut):
        result=f.result()
        results.append(result)
        print(result["status"],result["label"],str(result.get("actualWidth",""))+"x"+str(result.get("actualHeight","")),flush=True)
results.sort(key=lambda x:next(i for i,c in enumerate(choices) if c["url"]==x["url"]))
OUT.parent.mkdir(parents=True,exist_ok=True)
OUT.write_text(json.dumps({"type":"one_time_local_ffprobe_research_not_public_catalog","date":"2026-10-25","source":"william-ricchiuti/Live-Environment-Streams streams.geojson, locally downloaded","noTVTests":True,"privateFeasibilityResearch":True,"attempted":len(results),"fullHdOrAbove":sum(x["status"]=="verified_1080plus" for x in results),"results":results},ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
print("WROTE",OUT,"1080plus",sum(x["status"]=="verified_1080plus" for x in results),flush=True)
