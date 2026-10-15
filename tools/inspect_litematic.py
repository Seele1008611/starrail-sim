import gzip, struct, json, math
from pathlib import Path
from collections import Counter
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
class NBT:
    def __init__(self,data): self.data=data;self.i=0
    def read(self,n): p=self.data[self.i:self.i+n];self.i+=n;return p
    def number(self,f): return struct.unpack('>'+f,self.read(struct.calcsize(f)))[0]
    def string(self): return self.read(self.number('H')).decode('utf-8')
    def tag(self,t):
        if t in range(1,7):return self.number({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[t])
        if t==7:return self.read(self.number('i'))
        if t==8:return self.string()
        if t==9:
            k=self.number('B');return [self.tag(k) for _ in range(self.number('i'))]
        if t==10:
            d={}
            while True:
                k=self.number('B')
                if not k:return d
                name=self.string();d[name]=self.tag(k)
        if t in (11,12):return [self.number('i' if t==11 else 'q') for _ in range(self.number('i'))]
        raise ValueError(t)

def load():
    p=ROOT/'设计方案/模型资产/星穹列车.litematic';n=NBT(gzip.decompress(p.read_bytes()));t=n.number('B');n.string();d=n.tag(t)
    blocks={};palettes=[]
    for name,r in d['Regions'].items():
        sizes=[r['Size'][k] for k in 'xyz'];size=list(map(abs,sizes));pos=[r['Position'][k]+min(0,sizes[i]+1) for i,k in enumerate('xyz')]
        pal=r['BlockStatePalette'];bits=max(2,(len(pal)-1).bit_length());words=[v&((1<<64)-1) for v in r['BlockStates']];mask=(1<<bits)-1
        for i in range(math.prod(size)):
            bit=i*bits;word,off=divmod(bit,64);v=words[word]>>off
            if off+bits>64:v|=words[word+1]<<(64-off)
            pi=v&mask;b=pal[pi]
            if b['Name'].endswith((':air',':cave_air',':void_air')):continue
            x=i%size[0];z=i//size[0]%size[2];y=i//(size[0]*size[2]);blocks[(x+pos[0],y+pos[1],z+pos[2])]=b
        palettes.append({'name':name,'size':sizes,'pos':pos,'palette':pal})
    return d,blocks,palettes

if __name__=='__main__':
    d,b,p=load();out=ROOT/'设计方案/模型资产/litematic分析';out.mkdir(exist_ok=True)
    report={'metadata':d.get('Metadata'),'regions':p,'count':len(b),'bounds':[[min(q[i] for q in b),max(q[i] for q in b)] for i in range(3)],'materials':Counter(v['Name'] for v in b.values())}
    (out/'analysis.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps(report,ensure_ascii=False,default=str))
    for axes,label in [((2,1),'side'),((2,0),'top'),((0,1),'front')]:
        a,c=axes;lo=[min(q[i] for q in b) for i in range(3)];hi=[max(q[i] for q in b) for i in range(3)]
        img=Image.new('RGB',(hi[a]-lo[a]+1,hi[c]-lo[c]+1),(240,240,240))
        for q,v in sorted(b.items(),key=lambda t:t[0][3-a-c]):
            name=v['Name'];color=(60,82,96)
            if any(s in name for s in ['yellow','gold','orange','copper']):color=(205,158,66)
            elif any(s in name for s in ['white','quartz','iron','light_gray']):color=(181,185,185)
            elif any(s in name for s in ['black','coal']):color=(30,32,38)
            elif 'glass' in name:color=(103,159,179)
            img.putpixel((q[a]-lo[a],hi[c]-q[c]),color)
        img.resize((img.width*5,img.height*5),Image.Resampling.NEAREST).save(out/(label+'.png'))
