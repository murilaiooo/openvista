"""Create the OpenVista README cover artwork using Pillow (no external media)."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import math, random
root=Path(__file__).resolve().parents[1]
dst=root/"assets/branding/openvista-cover.png"
dst.parent.mkdir(parents=True,exist_ok=True)
W,H,S=1500,450,2
im=Image.new("RGB",(W*S,H*S))
px=im.load()
for y in range(H*S):
 t=y/(H*S)
 for x in range(W*S):
  u=x/(W*S)
  g=max(0,1-abs(u-0.79)/0.40)*max(0,1-abs(t-0.42)/0.69)
  px[x,y]=(int(7+10*u+29*g),int(19+29*u+38*g),int(38+44*u+25*g))
draw=ImageDraw.Draw(im,"RGBA")
def poly(points,color):draw.polygon([(int(x*S),int(y*S)) for x,y in points],fill=color)
# warm sun and silhouetted horizon
for radius in range(160,110,-5):
 alpha=max(0,min(80,int((165-radius)*1.2)))
 draw.ellipse(((1210-radius)*S,(181-radius)*S,(1210+radius)*S,(181+radius)*S),fill=(254,183,107,alpha))
draw.ellipse((1108*S,78*S,1312*S,282*S),fill=(246,190,126,255))
poly([(650,385),(820,251),(918,335),(1054,192),(1227,344),(1372,228),(1500,331),(1500,450),(650,450)],(42,94,111,255))
poly([(665,405),(889,314),(998,379),(1104,262),(1304,373),(1459,305),(1500,323),(1500,450),(665,450)],(25,68,87,255))
poly([(690,433),(918,371),(1060,424),(1261,350),(1500,392),(1500,450),(690,450)],(9,43,67,255))
random.seed(7)
for _ in range(55):
 x=random.randint(925,1495);y=random.randint(395,446);w=random.randint(9,55)
 draw.rounded_rectangle((x*S,y*S,(x+w)*S,(y+1)*S),radius=2*S,fill=(130,225,226,random.randint(22,110)))
# gradient readable left side
overlay=Image.new("RGBA",im.size)
po=overlay.load()
for x in range(W*S):
 alpha=round(248*max(0,1-(x/(W*S))/.68)**1.45)
 for y in range(H*S):po[x,y]=(7,19,37,alpha)
im=Image.alpha_composite(im.convert("RGBA"),overlay)
draw=ImageDraw.Draw(im,"RGBA")
fonts=Path("C:/Windows/Fonts")
def font(name,size):
 for p in [fonts/name,fonts/"segoeuib.ttf",fonts/"segoeui.ttf",fonts/"arial.ttf"]:
  if p.exists():return ImageFont.truetype(str(p),size*S)
 return ImageFont.load_default()
bold=font("segoeuib.ttf",82);semi=font("segoeuib.ttf",21);sub=font("segoeui.ttf",29);small=font("segoeui.ttf",20)
# distinctive minimalist landscape mark
draw.rounded_rectangle((72*S,77*S,122*S,127*S),radius=13*S,fill=(11,175,169,255))
draw.ellipse((101*S,88*S,111*S,98*S),fill=(250,223,177,255))
draw.line([(81*S,117*S),(93*S,104*S),(101*S,111*S),(111*S,99*S)],fill=(246,253,251,255),width=4*S)
draw.text((139*S,88*S),"OPEN / VISTA",font=semi,fill=(149,233,222,255))
draw.text((65*S,157*S),"OpenVista",font=bold,fill=(249,251,250,255))
draw.text((72*S,276*S),"A window to everywhere.",font=sub,fill=(226,239,244,255))
draw.text((75*S,343*S),"Turn the big screen into a beautiful moment.",font=small,fill=(163,209,218,255))
draw.rounded_rectangle((72*S,402*S,267*S,435*S),radius=15*S,outline=(67,168,175,150),width=2*S,fill=(9,51,65,190))
draw.text((90*S,407*S),"IN DEVELOPMENT",font=font("segoeui.ttf",16),fill=(159,236,221,255))
im.convert("RGB").resize((W,H),Image.Resampling.LANCZOS).save(dst,optimize=True)
print("Generated",dst, dst.stat().st_size,"bytes")
