"""Small direct-HLS urban and mountain probe, for local OpenVista feasibility only."""
import json, subprocess, concurrent.futures, pathlib, sys, threading
from urllib.parse import urlsplit
sys.stdout.reconfigure(encoding="utf-8", errors="replace")
ROOT=pathlib.Path(__file__).resolve().parents[1]
SOURCE=pathlib.Path.home()/"AppData/Local/Temp/openvista-streams.geojson"
OUT=ROOT/"docs/research/scenic-city-1080p-probe-results-2026-10-25.json"
TARGETS=[
("Cracóvia — praça principal","krakow_cam_702b61.stream/playlist.m3u8"),
("Varsóvia — praça do castelo","warszawa_cam_5f3b1a"),
("Wrocław — praça da cidade","wroclaw_przed_cam_16c68e"),
("Zakopane — rua central","krupowki2_cam_62169a"),
("Opole — praça","opole_cam_8b53d4"),
("Malbork — castelo","malbork_cam_0ffc99"),
("Florença — centro histórico","via-della-scala.stream"),
("Torre de TV — Tallinn","teletorn.smil"),
("Mississauga — praça da cidade","LiveStageWebcam.stream-audiodestin"),
("Whistler — vila","whistlervillagefitz"),
("Rzepiska — Tatras","airnet_cam_d60f3c"),
("Bukowina — hotel","bukovina_cam_7aa04f"),
("Karpacz — panorama","karpacz_cam_ab594f"),
("Wintergreen — montanhas","wintergreen2.stream"),
("Bryce Resort — montanhas","bryceresort.stream"),
("Massanutten Resort — cume","massresort3.stream"),
("Arachova — cidade grega","/live/arach1/"),
("Kraków — praça de Podgórze","krakow_cam_c44608"),
("Bishkek — panorama","cam.kt.kg/cam33"),
("Krakow — praça Rynek principal, outra câmera","krakow_cam_9a3b91")
]
data=json.loads(SOURCE.read_text(encoding="utf-8"))["features"]
props=[x["properties"] for x in data]
choices=[]
for label,key in TARGETS:
    hits=[p for p in props if key in (p.get("url") or "")]
    if len(hits)!=1:
        print("SKIP",label,"matches",len(hits),flush=True);continue
    p=hits[0];url=p["url"]
    if p.get("url_type")!="hls" or "token=" in url or p.get("source_url_requires"):
        print("SKIP auth/unsupported",label,flush=True);continue
    choices.append({"label":label,"url":url,"name":p.get("name"),"reportedResolution":p.get("resolution"),"source":p.get("source_family")})
locks={};guard=threading.Lock()
def test(c):
    with guard:
        locks.setdefault(urlsplit(c["url"]).netloc,threading.BoundedSemaphore(2))
        sem=locks[urlsplit(c["url"]).netloc]
    with sem:
        args=["ffprobe","-v","error","-rw_timeout","9000000","-analyzeduration","1000000","-probesize","500000","-select_streams","v:0","-show_entries","stream=codec_name,width,height","-of","json",c["url"]]
        try:
            res=subprocess.run(args,capture_output=True,encoding="utf-8",errors="replace",timeout=17)
            if res.returncode:
                return {**c,"status":"failed","reason":res.stderr.strip()[-150:]}
            streams=json.loads(res.stdout).get("streams",[])
            if not streams:
                return {**c,"status":"no_video"}
            x=streams[0];w=x.get("width",0);h=x.get("height",0)
            return {**c,"status":"verified_1080plus" if w>=1920 and h>=1080 else "verified_below_fullhd","actualWidth":w,"actualHeight":h,"codec":x.get("codec_name")}
        except subprocess.TimeoutExpired:
            return {**c,"status":"timeout"}
        except Exception as e:
            return {**c,"status":"error","reason":str(e)[:150]}
print("Testing",len(choices),"city/mountain streams",flush=True)
with concurrent.futures.ThreadPoolExecutor(max_workers=4) as ex:
    results=list(ex.map(test,choices))
for r in results: print(r["status"],r["label"],r.get("actualWidth",""),"x",r.get("actualHeight",""),flush=True)
OUT.write_text(json.dumps({"type":"one_time_local_ffprobe_research_not_public_catalog","date":"2026-10-25","attempted":len(results),"fullHdOrAbove":sum(r["status"]=="verified_1080plus" for r in results),"results":results},indent=2,ensure_ascii=False)+"\n",encoding="utf-8")
print("WROTE",OUT,flush=True)
