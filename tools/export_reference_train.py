"""Export the editable Blockbench meshes to the client mesh resource.

Run after saving Blockbench edits. Coordinates use 16 editor units per block;
smooth corner normals retain hard cap / panel edges above 45 degrees.
"""
import base64
import json
import math
from pathlib import Path


def export_model(root):
    source=root/'设计方案/遗迹设计/模型源文件/1.17.0/star_rail_train.bbmodel'
    bb=json.loads(source.read_text(encoding='utf-8'))
    out=[]
    def unit(v):
        length=math.sqrt(sum(t*t for t in v))
        return tuple(t/max(length,1e-12) for t in v)
    def rotate(v,angles):
        x,y,z=v
        for axis,degrees in enumerate(angles):
            a=math.radians(degrees);c,s=math.cos(a),math.sin(a)
            if axis==0:y,z=y*c-z*s,y*s+z*c
            elif axis==1:x,z=x*c+z*s,-x*s+z*c
            else:x,y=x*c-y*s,x*s+y*c
        return x,y,z
    for mesh in bb['elements']:
        if mesh.get('type')!='mesh' or mesh.get('export') is False:continue
        angles=mesh.get('rotation',[0,0,0]);origin=mesh.get('origin',[0,0,0])
        positions={k:tuple((v+o)/16 for v,o in zip(rotate(p,angles),origin)) for k,p in mesh['vertices'].items()}
        faces=[];adj={k:[] for k in positions}
        for face in mesh['faces'].values():
            ids=face['vertices']
            if len(ids)<3 or face.get('texture') is None:continue
            a,b,c=(positions[k] for k in ids[:3]);u=[b[i]-a[i] for i in range(3)];v=[c[i]-a[i] for i in range(3)]
            normal=unit((u[1]*v[2]-u[2]*v[1],u[2]*v[0]-u[0]*v[2],u[0]*v[1]-u[1]*v[0]))
            faces.append((face,normal))
            for k in ids:adj[k].append(normal)
        for face,n in faces:
            verts=[]
            for k in face['vertices']:
                ns=[q for q in adj[k] if sum(a*b for a,b in zip(n,q))>.707]
                smooth=unit(tuple(sum(q[i] for q in ns) for i in range(3)))
                uv=face['uv'][k]
                verts.append([*[round(v,6) for v in positions[k]],round(uv[0]/256,6),round(uv[1]/256,6),*[round(v,6) for v in smooth]])
            chunks=[verts] if len(verts)<=4 else [[verts[0],verts[i],verts[i+1]] for i in range(1,len(verts)-1)]
            for chunk in chunks:
                if len(chunk)==3:chunk=chunk+[chunk[-1]]
                out.append({'vertices':chunk,'emissive':'warm lens' in mesh['name'] or 'amber lens' in mesh['name'] or mesh['name']=='Schematic locomotive lamp'})
    assets=root/'src/main/resources/assets/starrail_sim'
    dest=assets/'models/train/star_rail_train_reference.json'
    dest.write_text(json.dumps({'bounds':bb.get('train_bounds',[0,0,0,6,8,20]),'texture':'starrail_sim:textures/block/star_rail_train_reference.png','quads':out},separators=(',',':')),encoding='utf-8')
    texture=bb['textures'][0]['source']
    (assets/'textures/block/star_rail_train_reference.png').write_bytes(base64.b64decode(texture.split(',',1)[1]))
    return len(out)


if __name__=='__main__':
    root=Path(__file__).resolve().parents[1]
    print(json.dumps({'exported_quads':export_model(root)}))
