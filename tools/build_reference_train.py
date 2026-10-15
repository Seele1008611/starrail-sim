"""Build the reference-based Astral Express mesh, editable Blockbench source and footprint.

Coordinates are blocks, +Z is the rear of the locomotive. Run from the repository root.
The client and Blockbench receive the same faces, so edits cannot diverge silently.
"""
from __future__ import annotations

import base64
import copy
import io
import json
import math
import pathlib
import random
import uuid
import zipfile
from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/starrail_sim'
SOURCE = ROOT / '设计方案/遗迹设计/模型源文件/1.17.0'
WIDTH, LENGTH, HEIGHT = 6, 20, 8
MATERIALS = [
    ('navy', (41, 92, 127)), ('blue', (49, 108, 143)), ('deep', (18, 41, 57)),
    ('gold', (199, 157, 85)), ('gold_light', (236, 199, 125)),
    ('bronze', (132, 91, 46)), ('silver', (181, 196, 200)),
    ('steel', (65, 77, 80)), ('rubber', (22, 25, 29)),
    ('window', (51, 105, 122)), ('lamp', (255, 224, 124)),
    ('ivory', (229, 227, 206)), ('rail', (84, 100, 110)),
    ('red', (95, 41, 44)), ('panel', (33, 54, 69)), ('glass', (55, 85, 96)),
]
MAT = {name: i for i, (name, _) in enumerate(MATERIALS)}


def add(a, b): return tuple(x+y for x, y in zip(a, b))
def sub(a, b): return tuple(x-y for x, y in zip(a, b))
def mul(a, s): return tuple(x*s for x in a)
def dot(a, b): return sum(x*y for x, y in zip(a, b))
def cross(a, b): return (a[1]*b[2]-a[2]*b[1], a[2]*b[0]-a[0]*b[2], a[0]*b[1]-a[1]*b[0])
def norm(a): return mul(a, 1/max(math.sqrt(dot(a,a)), 1e-9))


class Part:
    def __init__(self, name, group, material, glow=False):
        self.name, self.group, self.material, self.glow = name, group, MAT[material], glow
        self.vertices = []
        self.faces = []
        self.uuid = str(uuid.uuid5(uuid.NAMESPACE_URL, 'starrail/reference_train/' + name))
        PARTS.append(self)

    def vertex(self, v):
        self.vertices.append(tuple(round(x, 6) for x in v))
        return len(self.vertices)-1

    def face(self, ids, outward=None, normals=None):
        ids = list(ids)
        n = norm(cross(sub(self.vertices[ids[1]], self.vertices[ids[0]]),
                       sub(self.vertices[ids[2]], self.vertices[ids[0]])))
        if outward is not None and dot(n, outward) < 0:
            ids.reverse()
            if normals: normals = list(reversed(normals))
            n = mul(n, -1)
        if not normals: normals = [n] * len(ids)
        self.faces.append((ids, normals))


PARTS = []


def box(name, group, material, lo, hi, glow=False):
    p = Part(name, group, material, glow)
    for x,y,z in [(0,0,0),(1,0,0),(1,1,0),(0,1,0),(0,0,1),(1,0,1),(1,1,1),(0,1,1)]:
        p.vertex((hi[0] if x else lo[0], hi[1] if y else lo[1], hi[2] if z else lo[2]))
    for ids,n in [([0,3,2,1],(0,0,-1)),([4,5,6,7],(0,0,1)),
                  ([0,4,7,3],(-1,0,0)),([1,2,6,5],(1,0,0)),
                  ([0,1,5,4],(0,-1,0)),([3,7,6,2],(0,1,0))]: p.face(ids,n)
    return p


def tube(name, group, material, points, radius, sides=10, cap=True, glow=False):
    """Parallel-transport frames keep curved pipes continuous at every bend."""
    p = Part(name, group, material, glow)
    frames=[]; previous=None
    for i,pos in enumerate(points):
        tangent=norm(sub(points[min(i+1,len(points)-1)],points[max(i-1,0)]))
        if previous is None:
            helper=(0,1,0) if abs(tangent[1]) < .9 else (1,0,0)
            u=norm(cross(tangent,helper))
        else:
            u=norm(sub(previous,mul(tangent,dot(previous,tangent))))
        v=norm(cross(tangent,u));previous=u
        frames.append((tangent,u,v))
        r=radius[i] if isinstance(radius,list) else radius
        for j in range(sides):
            a=j*math.tau/sides
            p.vertex(add(pos,mul(add(mul(u,math.cos(a)),mul(v,math.sin(a))),r)))
    for i in range(len(points)-1):
        for j in range(sides):
            nj=(j+1)%sides
            ids=[i*sides+j,i*sides+nj,(i+1)*sides+nj,(i+1)*sides+j]
            normals=[]
            for ring,k in [(i,j),(i,nj),(i+1,nj),(i+1,j)]:
                _,u,v=frames[ring];a=k*math.tau/sides
                normals.append(add(mul(u,math.cos(a)),mul(v,math.sin(a))))
            p.face(ids,normals[0],normals)
    if cap:
        for i in [0,len(points)-1]:
            c=p.vertex(points[i]); outward=mul(frames[i][0],-1 if i==0 else 1)
            for j in range(sides):p.face([c,i*sides+j,i*sides+(j+1)%sides],outward)
    return p


def cylinder(name, group, material, start, end, radius, sides=32, glow=False):
    return tube(name,group,material,[start,end],radius,sides,True,glow)


def torus(name, group, material, center, radius, thickness, axis='z', sides=48):
    if axis=='z':pts=[(center[0]+radius*math.cos(a),center[1]+radius*math.sin(a),center[2]) for a in [j*math.tau/sides for j in range(sides+1)]]
    else:pts=[(center[0],center[1]+radius*math.cos(a),center[2]+radius*math.sin(a)) for a in [j*math.tau/sides for j in range(sides+1)]]
    return tube(name,group,material,pts,thickness,8,False)


def profile(name, group, material, center, rings, sides=48):
    p=Part(name,group,material)
    for z,r in rings:
        for j in range(sides):
            a=j*math.tau/sides;p.vertex((center[0]+r*math.cos(a),center[1]+r*math.sin(a),z))
    for k in range(len(rings)-1):
        dz=rings[k+1][0]-rings[k][0];dr=rings[k+1][1]-rings[k][1]
        for j in range(sides):
            nj=(j+1)%sides;ids=[k*sides+j,k*sides+nj,(k+1)*sides+nj,(k+1)*sides+j]
            ns=[norm((math.cos(q*math.tau/sides),math.sin(q*math.tau/sides),-dr/max(dz,1e-6))) for q in [j,nj,nj,j]]
            p.face(ids,ns[0],ns)
    for k in [0,len(rings)-1]:
        c=p.vertex((center[0],center[1],rings[k][0]));n=(0,0,-1 if k==0 else 1)
        for j in range(sides):p.face([c,k*sides+j,k*sides+(j+1)%sides],n)
    return p


def plate(name,group,material,xy,z0,z1):
    """Extrude a planar ornament. Fan triangulation is used for convex / star-shaped leaves."""
    p=Part(name,group,material)
    for z in [z0,z1]:
        for x,y in xy:p.vertex((x,y,z))
    n=len(xy);c0=p.vertex((sum(x for x,y in xy)/n,sum(y for x,y in xy)/n,z0));c1=p.vertex((sum(x for x,y in xy)/n,sum(y for x,y in xy)/n,z1))
    for j in range(n):
        k=(j+1)%n;p.face([c0,j,k],(0,0,-1));p.face([c1,j+n,k+n],(0,0,1))
        a,b=xy[j],xy[k];out=norm((b[1]-a[1],a[0]-b[0],0));p.face([j,j+n,k+n,k],out)
    return p


def build_geometry():
    box('Long alloy underframe','Chassis','deep',(.5,1.55,.5),(5.5,1.95,19.55))
    box('West running board','Chassis','navy',(.2,1.92,1.2),(1.05,2.13,19.2))
    box('East running board','Chassis','navy',(4.95,1.92,1.2),(5.8,2.13,19.2))
    for x in [.32,5.68]:tube(f'Running board brass {x}','Chassis','gold',[(x,2.15,z) for z in [1.2,4,8,12,16,19.2]],.035)
    # The broad round boiler defines the silhouette. There is no disconnected front plate.
    profile('Continuous navy cylindrical boiler','Boiler','navy',(3,4.08),[(.82,1.84),(1.45,2.03),(3.3,2.05),(10.9,2.02),(12.5,1.96),(13.85,1.82)])
    for z in [2.1,6.3,10.1]:
        torus(f'Polished boiler collar {z}','Boiler','silver',(3,4.08,z),2.047,.025)
        torus(f'Brass collar highlight {z}','Boiler','gold',(3,4.08,z+.065),2.045,.021)
    profile('Rear sculpted machinery collar','Boiler','steel',(3,4.08),[(12.5,1.98),(12.7,2.17),(13.35,2.17),(13.65,1.93)])
    for z in [12.71,13.34]:torus(f'Rear collar gold {z}','Boiler','gold',(3,4.08,z),2.17,.045)
    # Ornate circular front, with the silver trailblazer fan in a dark medallion.
    profile('Connected front circular shoulder','Front medallion','deep',(3,4.08),[(.45,1.77),(.68,1.94),(1.48,2.03)])
    cylinder('Dark medallion face','Front medallion','panel',(3,4.08,.20),(3,4.08,.48),1.68,64)
    torus('Outer substantial brass bezel','Front medallion','gold',(3,4.08,.33),1.81,.125,sides=64)
    torus('Bright silver bezel edge','Front medallion','silver',(3,4.08,.24),1.86,.033,sides=64)
    torus('Inner engraved circle','Front medallion','gold_light',(3,4.08,.135),1.46,.035,sides=64)
    # Eight curled ornaments are inset between the rims, rather than a plain ring.
    for k in range(8):
        a=k*math.tau/8+.12;cx=3+1.57*math.cos(a);cy=4.08+1.57*math.sin(a)
        pts=[]
        for j in range(24):
            t=j/23;ang=a-.8+t*math.pi*1.55;r=.24*(1-t*.75)
            pts.append((cx+r*math.cos(ang),cy+r*math.sin(ang),.09))
        tube(f'Engraved curling brass ornament {k}','Front medallion','gold',pts,.026,8)
    for k in range(12):
        a=k*math.tau/12
        cylinder(f'Bezel rivet {k}','Front medallion','gold_light',(3+1.78*math.cos(a),4.08+1.78*math.sin(a),.09),(3+1.78*math.cos(a),4.08+1.78*math.sin(a),.20),.052,12)
    petals=[[(3,3.04),(2.38,4.65),(2.18,5.00),(2.38,4.91),(2.66,4.71)],
            [(3,3.04),(2.69,4.74),(2.59,5.20),(2.80,5.08),(2.96,4.83)],
            [(3,3.04),(3.04,4.86),(3.12,5.34),(3.31,5.09),(3.27,4.75)],
            [(3,3.04),(3.34,4.67),(3.66,5.02),(3.85,4.82),(3.57,4.49)]]
    for i,p in enumerate(petals):
        plate(f'Fan crest gold backing {i}','Front medallion','gold',p,.025,.14)
        center=(3,4.1);inset=[(center[0]+(x-center[0])*.88,center[1]+(y-center[1])*.94) for x,y in p]
        plate(f'Silver fan emblem blade {i}','Front medallion','ivory',inset,-.004,.026)
    tube('Central emblem brass stem','Front medallion','gold_light',[(3,3.05,.001),(3.06,3.85,.001),(3.10,4.65,.001)],.024)
    # Top headlamp: a circular lens, supporting brass fan and swept blue visor.
    plate('Brass roof lamp mounting fan','Roof headlamp','gold',[(2.15,5.73),(3.85,5.73),(3.55,6.75),(3.24,7.05),(2.76,7.05),(2.45,6.75)],.92,1.48)
    cylinder('Roof lamp round metal housing','Roof headlamp','deep',(3,7.12,.59),(3,7.12,1.80),.54,40)
    torus('Roof lamp gold face ring','Roof headlamp','gold_light',(3,7.12,.53),.47,.07,sides=48)
    cylinder('Roof lamp amber lens','Roof headlamp','lamp',(3,7.12,.49),(3,7.12,.58),.38,48,True)
    torus('Roof lamp silver retaining rim','Roof headlamp','silver',(3,7.12,.455),.39,.022,sides=40)
    hood=Part('Swept streamlined roof lamp visor','Roof headlamp','blue')
    hoodrings=[(.31,7.58,.68),(1.0,7.76,.72),(2.1,7.28,.64),(3.7,6.80,.57),(5.3,6.28,.42)]
    for z,y,w in hoodrings:
        for j in range(9):
            a=j*math.pi/8;hood.vertex((3+w*math.cos(a),y+.18*math.sin(a),z))
    for i in range(len(hoodrings)-1):
        for j in range(8):hood.face([i*9+j,i*9+j+1,(i+1)*9+j+1,(i+1)*9+j],(0,1,0))
    for sign in [-1,1]:tube(f'Visor silver edge {sign}','Roof headlamp','silver',[(3+sign*w,y+.01,z) for z,y,w in hoodrings],.028)
    # Streamlined passenger-style cab at the rear, blended into the machinery collar.
    box('Cab lower navy hull','Rear cab','navy',(.85,1.98,13.6),(5.15,4.66,19.55))
    arch=Part('Arched blue rear cab roof','Rear cab','blue')
    sections=[(13.1,1.92),(13.65,2.23),(18.8,2.23),(19.7,1.94)]
    for z,r in sections:
        for j in range(25):
            a=j*math.pi/24;arch.vertex((3+r*math.cos(a),4.64+1.72*math.sin(a),z))
    for i in range(len(sections)-1):
        for j in range(24):arch.face([i*25+j,i*25+j+1,(i+1)*25+j+1,(i+1)*25+j],(0,1,0))
    for i in [0,len(sections)-1]:
        c=arch.vertex((3,4.64,sections[i][0]));n=(0,0,-1 if i==0 else 1)
        for j in range(24):arch.face([c,i*25+j,i*25+j+1],n)
    for x in [.82,5.18]:
        box(f'Cab long glass window {x}','Rear cab','window',(x-.025,3.63,14.7),(x+.025,4.52,18.6))
        for y in [3.60,4.56]:tube(f'Cab window gold rail {x} {y}','Rear cab','gold',[(x,y,14.6),(x,y,18.72)],.038)
        for z in [14.61,16.35,18.72]:tube(f'Cab window mullion {x} {z}','Rear cab','silver',[(x,3.61,z),(x,4.55,z)],.035)
        tube(f'Cab long lower silver stripe {x}','Rear cab','silver',[(x,2.8,13.7),(x,2.8,19.45)],.055)
        tube(f'Cab flowing upper gold stripe {x}','Rear cab','gold',[(x,3.05,13.4),(x,3.20,15),(x,3.18,18.8),(x,3.65,19.4)],.035)
    box('Rear fascia','Rear cab','deep',(1.15,2.0,19.5),(4.85,4.64,19.8))
    box('Rear tall window','Rear cab','window',(1.72,3.2,19.805),(4.28,4.43,19.83))
    # Paired side pipes, couplings, maker's plates and the front gold star.
    for sign in [-1,1]:
        x=3+sign*2.13
        tube(f'Long bent gold boiler pipe {sign}','Pipes and trim','gold',[(x,3.72,1.5),(x,3.10,1.85),(x,2.83,2.5),(x,2.84,8.8),(x,2.83,11.5),(x,2.38,12.25),(x,2.14,13.2)],.065,12)
        tube(f'Lower silver steam pipe {sign}','Pipes and trim','silver',[(x+sign*.18,2.33,2.4),(x+sign*.18,2.2,3),(x+sign*.18,2.2,7.8),(x+sign*.18,1.8,9),(x+sign*.18,1.9,11.3)],.11,12)
        for z in [4,7,10]:cylinder(f'Brass pipe sleeve {sign} {z}','Pipes and trim','gold_light',(x,2.84,z-.12),(x,2.84,z+.12),.09,16)
        platexy=[]
        for j in range(33):
            a=j*math.tau/32;platexy.append((4.45+.13*math.sin(a),5.25+.6*math.cos(a)))
        pts=[(3+sign*2.025,y,z) for y,z in platexy]
        tube(f'Silver maker plate outline {sign}','Pipes and trim','silver',pts,.021,8,False)
        starx=3+sign*2.045
        for ang in [0,math.pi/2,math.pi/4,-math.pi/4]:
            dy=.18*math.cos(ang);dz=.18*math.sin(ang)
            tube(f'Boiler gold star {sign} {ang}','Pipes and trim','gold_light',[(starx,4.3-dy,2.6-dz),(starx,4.3+dy,2.6+dz)],.035,6)
        # Two independent lower headlamp pods on each side.
        for row,(cx,cy,cz,r) in enumerate([(3+sign*2.1,2.66,1.18,.33),(3+sign*2.05,3.45,1.0,.28)]):
            profile(f'Swept lower headlamp pod {sign} {row}','Auxiliary lamps','navy',(cx,cy),[(cz-.12,r+.18),(cz+.45,r+.20),(cz+1.5,r*.67)],32)
            torus(f'Lower lamp brass bezel {sign} {row}','Auxiliary lamps','gold',(cx,cy,cz-.15),r+.018,.065,sides=32)
            cylinder(f'Lower lamp warm lens {sign} {row}','Auxiliary lamps','lamp',(cx,cy,cz-.21),(cx,cy,cz-.10),r*.86,32,True)
    # Five wheel pairs with metal rims, hubs and radial spokes.
    for wi,z in enumerate([4.0,7.3,10.6,14.9,18.0]):
        r=1.14 if wi<3 else .88;cy=r+.075
        for sign in [-1,1]:
            cx=3+sign*2.44;face=cx+sign*.23
            cylinder(f'Wheel tyre {wi} {sign}','Wheels','rubber',(cx-.22,cy,z),(cx+.22,cy,z),r,40)
            cylinder(f'Wheel recessed plate {wi} {sign}','Wheels','steel',(face-sign*.035,cy,z),(face+sign*.008,cy,z),r*.83,32)
            torus(f'Wheel polished rim {wi} {sign}','Wheels','silver',(face,cy,z),r*.9,.052,'x',40)
            for j in range(10):
                a=j*math.tau/10
                tube(f'Wheel spoke {wi} {sign} {j}','Wheels','deep',[(face,cy+.18*math.cos(a),z+.18*math.sin(a)),(face,cy+r*.79*math.cos(a),z+r*.79*math.sin(a))],.055,6)
            cylinder(f'Wheel gold hub {wi} {sign}','Wheels','gold',(face-sign*.06,cy,z),(face+sign*.09,cy,z),r*.22,24)
    for sign in [-1,1]:
        x=3+sign*2.76
        tube(f'Long wheel connecting rod {sign}','Wheels','silver',[(x,1.10,3.9),(x,1.06,7.3),(x,1.10,10.7)],.10,12)
        tube(f'Piston return arm {sign}','Wheels','gold',[(x,1.12,4.0),(x,1.53,2.35),(x,1.60,1.8)],.095,12)
        for z in [4,7.3,10.6]:cylinder(f'Connecting rod bearing {sign} {z}','Wheels','gold_light',(x-.04,1.10,z),(x+.04,1.10,z),.17,16)
    # Wide sloping front skirt and polished ribs, integrated with the deck.
    p=Part('Sculpted front cowcatcher','Chassis','navy')
    verts=[(.16,.32,.08),(3,.15,.025),(5.84,.32,.08),(.40,1.76,1.55),(3,1.88,1.3),(5.6,1.76,1.55),(.40,.25,1.6),(5.6,.25,1.6)]
    for v in verts:p.vertex(v)
    for ids,n in [([0,1,4,3],(-.1,.7,-1)),([1,2,5,4],(.1,.7,-1)),([0,3,6],(-1,0,0)),([2,7,5],(1,0,0)),([0,6,7,2],(0,-1,0)),([3,4,5,7],(0,0,1))]:p.face(ids,n)
    for x in [.7,1.35,2,4,4.65,5.3]:tube(f'Front skirt metal rib {x}','Chassis','silver',[(x,.35,.16),(x,1.57,1.44)],.038)
    box('Front central coupling','Chassis','steel',(2.65,.65,.03),(3.35,1.14,.55))
    tube('Front gold lower lip','Chassis','gold_light',[(.2,.33,.07),(1.8,.23,.025),(3,.17,.01),(4.2,.23,.025),(5.8,.33,.07)],.048,10)


def backup():
    dest=SOURCE/'backups/star_rail_train_compact_before_reference_rebuild.zip'
    if dest.exists():return
    dest.parent.mkdir(parents=True,exist_ok=True)
    paths=[SOURCE/'star_rail_train.bbmodel',ASSETS/'blockstates/star_rail_train.json',
           ASSETS/'models/item/star_rail_train.json',ASSETS/'textures/block/star_rail_train.png',
           ]
    paths+=list((ASSETS/'models/block').glob('star_rail_train_*.json'))
    with zipfile.ZipFile(dest,'w',zipfile.ZIP_DEFLATED) as z:
        for p in paths:
            if p.exists():z.write(p,p.relative_to(ROOT))


def atlas():
    img=Image.new('RGBA',(256,256));rnd=random.Random(117)
    for i,(name,color) in enumerate(MATERIALS):
        ox=(i%4)*64;oy=(i//4)*64
        for y in range(64):
            for x in range(64):
                # Broad restrained highlight; no noisy speckled placeholder palette.
                stripe=math.cos((x-24)*math.pi/64)*3 + math.sin(y*math.pi/64)*2
                if name in {'gold','gold_light','silver','steel','bronze'}:stripe*=1.7
                jitter=rnd.choice([-1,0,0,0,1])
                c=tuple(max(0,min(255,round(v+stripe+jitter))) for v in color)
                img.putpixel((ox+x,oy+y),c+(255,))
    path=ASSETS/'textures/block/star_rail_train_reference.png';img.save(path)
    buf=io.BytesIO();img.save(buf,format='PNG');return 'data:image/png;base64,'+base64.b64encode(buf.getvalue()).decode()


def export():
    embedded=atlas();bbels=[];quads=[];groups={}
    for p in PARTS:
        mi=p.material;u0=(mi%4)*64+7;v0=(mi//4)*64+7
        faces={}
        for fi,(ids,ns) in enumerate(p.faces):
            uv=[(u0,v0+49),(u0+49,v0+49),(u0+49,v0),(u0,v0)][:len(ids)]
            faces[str(fi)]={'vertices':[str(i) for i in ids],'uv':{str(i):list(t) for i,t in zip(ids,uv)},'texture':0}
            verts=[]
            for i,n,t in zip(ids,ns,uv):verts.append([*p.vertices[i],round(t[0]/256,6),round(t[1]/256,6),*[round(x,6) for x in n]])
            if len(verts)==3:verts.append(copy.deepcopy(verts[-1]))
            quads.append({'vertices':verts,'emissive':p.glow})
        bbels.append({'name':p.name,'uuid':p.uuid,'type':'mesh','origin':[0,0,0],
                      'vertices':{str(i):[round(x*16,5) for x in v] for i,v in enumerate(p.vertices)},'faces':faces})
        groups.setdefault(p.group,[]).append(p.uuid)
    texture={'name':'star_rail_train_reference','id':'0','width':256,'height':256,'uv_width':256,'uv_height':256,'source':embedded,'internal':True,'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,'starrail/reference_train_texture'))}
    bb={'meta':{'format_version':'5.0','model_format':'free','box_uv':False},'name':'Astral Express - Reference Rebuild',
        'model_identifier':'star_rail_train_reference','resolution':{'width':256,'height':256},'elements':bbels,
        'outliner':[{'name':g,'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,'starrail/train_group/'+g)),'origin':[0,0,0],'children':ids} for g,ids in groups.items()],
        'textures':[texture]}
    (SOURCE/'star_rail_train.bbmodel').write_text(json.dumps(bb,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
    model=ASSETS/'models/train/star_rail_train_reference.json';model.parent.mkdir(parents=True,exist_ok=True)
    model.write_text(json.dumps({'bounds':[0,0,0,WIDTH,HEIGHT,LENGTH],'texture':'starrail_sim:textures/block/star_rail_train_reference.png','quads':quads},separators=(',',':')),encoding='utf-8')
    (SOURCE/'star_rail_train_reference_manifest.json').write_text(json.dumps({'reference':'崩铁/物品/星穹列车.jpg','size_blocks':[WIDTH,LENGTH,HEIGHT],'parts':len(PARTS),'quads':len(quads),'groups':list(groups)},ensure_ascii=False,indent=2),encoding='utf-8')
    return len(quads)


def collision():
    """Coarse solid hulls follow the visible silhouette; empty space is not filled with blocks."""
    hulls=[]
    def hull(lo,hi):hulls.append((lo,hi))
    hull((.1,.05,0),(5.9,1.9,1.8))
    hull((.3,1.55,1.7),(5.7,2.15,19.7))
    # Quarter-block strips approximate a cylindrical collision cross-section.
    for i in range(16):
        y=2.08+i*.25;edge=max(abs(y-4.08),abs(y+.25-4.08));half=math.sqrt(max(0,2.02**2-edge**2))
        if half>.05:hull((3-half,y,.3),(3+half,y+.25,13.7))
    hull((.8,2.15,13.6),(5.2,6.35,19.8))
    hull((2.2,5.9,.3),(3.8,7.94,2.1))
    hull((2.35,6.5,2.1),(3.65,7.35,3.7))
    hull((2.5,6.1,3.7),(3.5,6.85,5.4))
    for sign in [-1,1]:
        cx=3+sign*2.05
        hull((cx-.48,3.1,.7),(cx+.48,3.9,2.5))
        cx=3+sign*2.1
        hull((cx-.52,2.15,.85),(cx+.52,3.2,2.8))
    for x in [.18,5.18]:
        for z in [4,7.3,10.6,14.9,18]:hull((x,0,z-1.1),(x+.65,2.35,z+1.1))
    cells={}
    for lo,hi in hulls:
        for z in range(max(0,math.floor(lo[2])),min(LENGTH,math.ceil(hi[2]))):
            for y in range(max(0,math.floor(lo[1])),min(HEIGHT,math.ceil(hi[1]))):
                for x in range(max(0,math.floor(lo[0])),min(WIDTH,math.ceil(hi[0]))):
                    a=[round(max(lo[j]-v,0),5) for j,v in enumerate((x,y,z))];b=[round(min(hi[j]-v,1),5) for j,v in enumerate((x,y,z))]
                    if min(b[j]-a[j] for j in range(3))>0:
                        cid=x+WIDTH*(y+HEIGHT*z);cells.setdefault(cid,[]).append(a+b)
    dest=ROOT/'src/main/resources/data/starrail_sim/train/star_rail_train_collision.json';dest.parent.mkdir(parents=True,exist_ok=True)
    dest.write_text(json.dumps({'width':WIDTH,'length':LENGTH,'height':HEIGHT,'cells':[{'id':i,'boxes':boxes} for i,boxes in sorted(cells.items())]},separators=(',',':')),encoding='utf-8')
    return len(cells)


if __name__=='__main__':
    backup();build_geometry();quads=export();cells=collision()
    from export_reference_train import export_model
    quads=export_model(ROOT)
    print(json.dumps({'parts':len(PARTS),'quads':quads,'occupied_cells':cells,'dimensions':[WIDTH,LENGTH,HEIGHT],'source':str(SOURCE/'star_rail_train.bbmodel')},ensure_ascii=False))
