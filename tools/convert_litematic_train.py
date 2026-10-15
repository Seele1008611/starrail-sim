"""Extract the locomotive only; read vanilla geometry, never vanilla textures.

The original schematic is read-only. Stable 6x8 cell strides preserve saved links.
"""
import json, math, zipfile, functools
from collections import defaultdict
from inspect_litematic import ROOT,load
import build_reference_train as build

SOURCE=build.SOURCE
SCALE=.2
CROP=(194,258)
JAR=zipfile.ZipFile('C:/Users/hlb/.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar')

@functools.lru_cache(None)
def resource(path):
    return json.loads(JAR.read('assets/minecraft/'+path))

@functools.lru_cache(None)
def model(name):
    d=resource('models/'+name.split(':')[-1]+'.json')
    if 'elements' not in d and 'parent' in d:d={**model(d['parent']),**d}
    return d

def matches(condition,props):
    if 'OR' in condition:return any(matches(c,props) for c in condition['OR'])
    if 'AND' in condition:return all(matches(c,props) for c in condition['AND'])
    return all(props.get(k) in v.split('|') for k,v in condition.items())

def variants(block):
    name=block['Name'].split(':')[-1];props=block.get('Properties',{})
    try:state=resource('blockstates/'+name+'.json')
    except KeyError:return []
    out=[]
    for key,value in state.get('variants',{}).items():
        if matches(dict(v.split('=') for v in key.split(',') if v),props):out.append(value[0] if isinstance(value,list) else value);break
    for part in state.get('multipart',[]):
        if matches(part.get('when',{}),props):
            v=part['apply'];out.append(v[0] if isinstance(v,list) else v)
    return out

def rotate(v,axis,angle,origin=(8,8,8)):
    p=[v[i]-origin[i] for i in range(3)];a=math.radians(angle);c,s=math.cos(a),math.sin(a)
    if axis=='x':p[1],p[2]=p[1]*c-p[2]*s,p[1]*s+p[2]*c
    elif axis=='y':p[0],p[2]=p[0]*c+p[2]*s,-p[0]*s+p[2]*c
    else:p[0],p[1]=p[0]*c-p[1]*s,p[0]*s+p[1]*c
    return [p[i]+origin[i] for i in range(3)]

def material(name):
    if any(s in name for s in ('lantern','shroomlight','end_rod','torch','yellow_stained')):return 'lamp'
    if 'glass' in name:return 'window'
    if any(s in name for s in ('oak','birch','sandstone','granite','yellow','bell','copper')):return 'gold_light' if any(s in name for s in ('birch','sandstone')) else 'gold'
    if any(s in name for s in ('white','quartz','calcite','diorite','iron','smooth_stone')):return 'silver'
    if any(s in name for s in ('black','coal')):return 'rubber'
    if 'gray' in name:return 'steel'
    if 'red' in name:return 'bronze'
    return 'navy'

FACES={'north':([0,3,2,1],(0,0,-1)),'south':([4,5,6,7],(0,0,1)),
       'west':([0,4,7,3],(-1,0,0)),'east':([1,2,6,5],(1,0,0)),
       'down':([0,1,5,4],(0,-1,0)),'up':([3,7,6,2],(0,1,0))}

def main():
    backup=SOURCE/'backups/star_rail_train_before_litematic_conversion.zip'
    if not backup.exists():
        paths=[SOURCE/'star_rail_train.bbmodel',SOURCE/'star_rail_train_reference_manifest.json',SOURCE/'star_rail_train_reference_preview.png',build.ASSETS/'models/train/star_rail_train_reference.json',build.ASSETS/'textures/block/star_rail_train_reference.png',ROOT/'src/main/resources/data/starrail_sim/train/star_rail_train_collision.json']
        paths+=list((ROOT/'src/main/java/com/starrail/sim').rglob('*Train*.java'))
        with zipfile.ZipFile(backup,'w',zipfile.ZIP_DEFLATED) as z:
            for p in paths:
                if p.exists():z.write(p,p.relative_to(ROOT))
    data,allblocks,_=load();blocks={q:b for q,b in allblocks.items() if CROP[0]<=q[0]<=CROP[1]}
    lo=[min(q[i] for q in blocks) for i in range(3)];hi=[max(q[i] for q in blocks)+1 for i in range(3)]
    dims=[(hi[2]-lo[2])*SCALE,(hi[1]-lo[1])*SCALE,(hi[0]-lo[0])*SCALE]
    build.WIDTH,build.HEIGHT,build.LENGTH=map(math.ceil,dims)
    parts={};solid=set();geometry={};unsupported=set()
    for q,b in blocks.items():
        elements=[]
        for v in variants(b):
            try:m=model(v['model'])
            except KeyError:unsupported.add(b['Name']);continue
            for e in m.get('elements',[]):elements.append((e,v))
        geometry[q]=elements
        if any(e['from']==[0,0,0] and e['to']==[16,16,16] and not e.get('rotation') for e,v in elements):solid.add(q)
    hulls=[];facecount=0
    for q,b in blocks.items():
        mat=material(b['Name']);part=parts.get(mat)
        if part is None:part=parts[mat]=build.Part('Schematic locomotive '+mat,'Locomotive',mat,mat=='lamp')
        for e,v in geometry[q]:
            vertices=[]
            for x,y,z in [(0,0,0),(1,0,0),(1,1,0),(0,1,0),(0,0,1),(1,0,1),(1,1,1),(0,1,1)]:
                p=[e['to'][i] if flag else e['from'][i] for i,flag in enumerate((x,y,z))]
                if 'rotation' in e:
                    r=e['rotation'];p=rotate(p,r['axis'],r['angle'],r['origin'])
                    if r.get('rescale'):
                        f=1/math.cos(math.radians(r['angle']));p=[r['origin'][i]+(p[i]-r['origin'][i])*(1 if 'xyz'[i]==r['axis'] else f) for i in range(3)]
                p=rotate(p,'x',-v.get('x',0));p=rotate(p,'y',-v.get('y',0))
                a=[q[i]+p[i]/16 for i in range(3)]
                vertices.append(((a[2]-lo[2])*SCALE,(a[1]-lo[1])*SCALE,(hi[0]-a[0])*SCALE))
            hulls.append(([min(p[i] for p in vertices) for i in range(3)],[max(p[i] for p in vertices) for i in range(3)]))
            for direction,face in e.get('faces',{}).items():
                ids,n=FACES[direction]
                if 'cullface' in face:
                    normal=FACES[face['cullface']][1];normal=rotate(normal,'x',-v.get('x',0),(0,0,0));normal=rotate(normal,'y',-v.get('y',0),(0,0,0))
                    neighbor=tuple(q[i]+round(normal[i]) for i in range(3))
                    if neighbor in solid:continue
                # Coordinate mapping is a proper rotation; vanilla face winding survives.
                indices=[part.vertex(vertices[i]) for i in ids];part.face(indices);facecount+=1
    # Inset closure follows the actual cut cross-section, leaving its original gold
    # rim visible. Close the lower chassis separately instead of hanging a square
    # facade across the circular body. Bands continue through every vertical gap.
    profile=[(.40,.60,1.02,3.58),(.60,.80,.86,3.74),
             (.80,1.20,1.35,3.25),(1.20,1.60,1.62,2.98),
             (1.60,1.80,1.44,3.16),(1.80,2.80,1.24,3.36),
             (2.80,3.00,1.44,3.16),(3.00,3.20,1.64,2.96),
             (3.20,3.60,1.84,2.76)]
    closing=[(f'Inset rear hull band {i}','navy',(x0,y0,12.78),(x1,y1,12.94))
             for i,(y0,y1,x0,x1) in enumerate(profile)]
    closing += [('Lower rear sill','steel',(1.35,.80,12.94),(3.25,.92,12.98)),
                ('Rear chassis gold belt','gold',(1.63,1.30,12.94),(2.97,1.36,12.97)),
                ('Rear center latch','silver',(2.22,2.10,12.94),(2.38,2.42,12.97)),
                ('Rear latch gold foot','gold',(2.16,2.06,12.94),(2.44,2.13,12.98))]
    for name,mat,a,b in closing:
        build.box(name,'Cabin closure',mat,a,b);hulls.append((a,b))
    build.torus('Inset rear service hatch rim','Cabin closure','gold',
                (2.3,2.26,12.953),.56,.025,sides=32)
    from optimize_train_faces import optimize
    optimization=optimize(build);facecount=optimization['after']
    build.export()
    modelpath=build.ASSETS/'models/train/star_rail_train_reference.json'
    runtime=json.loads(modelpath.read_text());runtime['bounds']=[0,0,0,*dims];modelpath.write_text(json.dumps(runtime,separators=(',',':')))
    # Collision follows each model element, consolidated to one hull per occupied cell.
    cells={}
    for a,b in hulls:
        for z in range(max(0,math.floor(a[2])),min(build.LENGTH,math.ceil(b[2]))):
            for y in range(max(0,math.floor(a[1])),min(build.HEIGHT,math.ceil(b[1]))):
                for x in range(max(0,math.floor(a[0])),min(build.WIDTH,math.ceil(b[0]))):
                    aa=[max(0,a[i]-p) for i,p in enumerate((x,y,z))];bb=[min(1,b[i]-p) for i,p in enumerate((x,y,z))]
                    if min(bb[i]-aa[i] for i in range(3))<=1e-6:continue
                    cid=x+6*(y+8*z)
                    if cid not in cells:cells[cid]=aa+bb
                    else:cells[cid]=[min(cells[cid][i],aa[i]) for i in range(3)]+[max(cells[cid][i+3],bb[i]) for i in range(3)]
    path=ROOT/'src/main/resources/data/starrail_sim/train/star_rail_train_collision.json'
    path.write_text(json.dumps({'width':build.WIDTH,'length':build.LENGTH,'height':build.HEIGHT,'cells':[{'id':i,'boxes':[box]} for i,box in sorted(cells.items())]},separators=(',',':')))
    # Flat material atlas: geometry lighting supplies shading, no block texture noise.
    from PIL import Image
    import io,base64
    img=Image.new('RGBA',(256,256))
    for i,(_,color) in enumerate(build.MATERIALS):img.paste(color+(255,),((i%4)*64,(i//4)*64,(i%4+1)*64,(i//4+1)*64))
    img.save(build.ASSETS/'textures/block/star_rail_train_reference.png');buf=io.BytesIO();img.save(buf,format='PNG')
    bbpath=SOURCE/'star_rail_train.bbmodel';bb=json.loads(bbpath.read_text(encoding='utf-8'));bb['name']='Astral Express - Litematic Locomotive';bb['train_bounds']=[0,0,0,*dims];bb['textures'][0]['source']='data:image/png;base64,'+base64.b64encode(buf.getvalue()).decode();bbpath.write_text(json.dumps(bb,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
    report={'source':'设计方案/模型资产/星穹列车.litematic','author':data['Metadata'].get('Author'),'crop_x_inclusive':CROP,'source_blocks':len(blocks),'scale':SCALE,'size_blocks':dims,'placement':[build.WIDTH,build.LENGTH,build.HEIGHT],'quads':facecount,'optimization':optimization,'rear_closed':True,'occupied_cells':len(cells),'unsupported':sorted(unsupported)}
    (SOURCE/'star_rail_train_reference_manifest.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8');print(json.dumps(report,ensure_ascii=False))

if __name__=='__main__':main()
