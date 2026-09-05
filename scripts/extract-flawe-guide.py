#!/usr/bin/env python3
"""Reproducible extraction of the English objective selector and text pairs.

The selector has no loops. Symbolic RAM reads explore both branch outcomes;
constant branches and MIPS delay slots are evaluated to collect every pair.
No ROM or BIOS is required. Translations are preserved when English matches.
"""
import argparse, struct, json
from pathlib import Path
parser = argparse.ArgumentParser(description="Extract Flawe 2.0's read-only guide selector from a user-supplied PPF30 patch.")
parser.add_argument("ppf", type=Path)
parser.add_argument("--output", type=Path, default=Path(__file__).resolve().parents[1] / "dw2003-dual-screen/app/src/main/assets/guide")
args = parser.parse_args()
patch = args.ppf.read_bytes()
if patch[:5] != b"PPF30" or patch[57] or patch[58]:
    raise SystemExit("Expected PPF30 without block-check or undo data")
m = {}
i = 60
while i + 9 <= len(patch):
    raw, length = struct.unpack_from("<QB", patch, i)
    i += 9
    if i + length > len(patch):
        raise SystemExit("Truncated PPF record")
    for j, value in enumerate(patch[i:i+length]):
        sector, within = divmod(raw+j, 2352)
        if 24 <= within < 2072:
            m[sector*2048+within-24] = value
    i += length
base=0x21cee000
blob=bytes(m.get(base+i,0) for i in range(0x9000))
def word(pc): return struct.unpack_from('<I',blob,pc)[0]
def signed(n): return (n+0x80000000)%0x100000000-0x80000000
def step(pc,r):
 w=word(pc);op=w>>26;rs=w>>21&31;rt=w>>16&31;rd=w>>11&31;sh=w>>6&31;fn=w&63;imm=w&65535;s=imm if imm<32768 else imm-65536
 targets=None
 if op in (4,5,1):
  v=r[rs];q=r[rt]
  known=v is not None and (op==1 or q is not None)
  cond=((v==q) if op==4 else (v!=q) if op==5 else ((v>=0) if rt==1 else (v<0))) if known else None
  targets=([pc+4+s*4] if cond else [pc+8]) if known else [pc+4+s*4,pc+8]
 elif op==0:
  if fn==0:r[rd]=None if r[rt] is None else signed(r[rt]<<sh)
  elif fn==3:r[rd]=None if r[rt] is None else r[rt]>>sh
  elif fn==37:r[rd]=None if r[rs] is None or r[rt] is None else r[rs]|r[rt]
  elif fn==33:r[rd]=None if r[rs] is None or r[rt] is None else signed(r[rs]+r[rt])
  else:raise ValueError((hex(pc),hex(w)))
 elif op==15:r[rt]=signed(imm<<16)
 elif op==9:r[rt]=None if r[rs] is None else signed(r[rs]+s)
 elif op==12:r[rt]=None if r[rs] is None else r[rs]&imm
 elif op==11:r[rt]=None if r[rs] is None else int((r[rs]&0xffffffff)<(s&0xffffffff))
 elif op in (32,35,36,37):r[rt]=None
 else:raise ValueError((hex(pc),hex(w)))
 r[0]=0
 return targets
todo=[(0xc68,tuple([0]*32))];seen=set();triples=set()
while todo:
 pc,rs=todo.pop()
 if (pc,rs) in seen:continue
 seen.add((pc,rs))
 if pc==0xd4c:
  triples.add(tuple(rs[3:6]));continue
 if pc in (0xd58,0xbfc):continue
 r=list(rs);targets=step(pc,r)
 if targets is not None:
  assert step(pc+4,r) is None
  todo.extend((p,tuple(r)) for p in targets)
 else:todo.append((pc+4,tuple(r)))
def decode(at):
 out='';i=at
 esc={1:' ',2:',',3:'.',7:':',0x13:"'",0x14:'"',0x15:'(',0x16:')',0x1a:'-'}
 while blob[i]:
  c=blob[i];i+=1
  if 4<=c<=13:out+=chr(48+c-4)
  elif 14<=c<=39:out+=chr(65+c-14)
  elif 40<=c<=65:out+=chr(97+c-40)
  elif c==1:out+=esc.get(blob[i],'');i+=1
  elif c==2:i+=1
  elif c==0xe7:out+='!'
  elif c==0xe6:out+='?'
 return out.strip()
entries={f'{a:04x}-{b:04x}':{'title':decode(t),'en':' '.join(filter(None,[decode(a),decode(b)]))} for t,a,b in sorted(triples)}
args.output.mkdir(parents=True, exist_ok=True)
json_path = args.output / "objectives.json"
if json_path.exists():
    previous = json.loads(json_path.read_text(encoding="utf-8"))
    for key, value in entries.items():
        old = previous.get(key, {})
        if old.get("en") == value["en"]:
            value.update({k:v for k,v in old.items() if k in ("es", "fr", "de", "it")})
json_path.write_text(json.dumps(entries,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
(args.output / "selector.bin").write_bytes(blob[0xc68:0x2724])
print(f"Extracted {len(entries)} objective pairs and {0x2724-0xc68} selector bytes")
