"""Decode exactly one video frame from four live HLS test candidates, no recording."""
import concurrent.futures, json, pathlib, subprocess, sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
ROOT=pathlib.Path(__file__).resolve().parents[1]
BASE=ROOT/"docs/research"
files=[BASE/"scenic-1080p-probe-results-2026-10-25.json",BASE/"scenic-city-1080p-probe-results-2026-10-25.json"]
allrows=[r for f in files for r in json.loads(f.read_text(encoding="utf-8"))["results"]]
targets=["Nazaré — Praia do Norte","Kalamata — Beach (Greece)","Sopot — Beach","Warsaw — Castle Square"]
def test(label):
    found=next(r for r in allrows if r["label"]==label and r["status"]=="verified_1080plus")
    cmd=["ffmpeg","-nostdin","-hide_banner","-loglevel","error","-rw_timeout","9000000","-i",found["url"],"-frames:v","1","-an","-sn","-f","null","-"]
    try:
        p=subprocess.run(cmd,capture_output=True,timeout=25,encoding="utf-8",errors="replace")
        return {"name":label,"url":found["url"],"resolution":f'{found["actualWidth"]}x{found["actualHeight"]}',"oneFrameDecoded":p.returncode==0,"error":p.stderr.strip()[-250:] if p.returncode else None}
    except subprocess.TimeoutExpired:
        return {"name":label,"oneFrameDecoded":False,"error":"25s timeout"}
with concurrent.futures.ThreadPoolExecutor(max_workers=4) as ex:
    results=list(ex.map(test,targets))
for r in results: print("DECODE_OK" if r["oneFrameDecoded"] else "DECODE_FAILED",r["name"],r.get("error",""),flush=True)
(BASE/"scenic-sample-frame-decode-2026-10-25.json").write_text(json.dumps({"scope":"four individual decoded frames on Windows PC, not Hisense test, no video recorded","results":results},ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
