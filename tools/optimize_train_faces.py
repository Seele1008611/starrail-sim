"""Exact union of coplanar rectangles with flat materials; cancel shared inner faces."""
from collections import defaultdict
import numpy as np

def optimize(build):
    planes=defaultdict(list);other=[];before=sum(len(p.faces) for p in build.PARTS)
    for p in build.PARTS:
        for ids,normals in p.faces:
            vs=[p.vertices[i] for i in ids]
            axis=next((i for i in range(3) if max(v[i] for v in vs)-min(v[i] for v in vs)<1e-5),None)
            if axis is None or len(vs)!=4:other.append((p,vs,normals));continue
            ab=[i for i in range(3) if i!=axis]
            if len({round(v[ab[0]],5) for v in vs})!=2 or len({round(v[ab[1]],5) for v in vs})!=2:
                other.append((p,vs,normals));continue
            lo=[round(min(v[i] for v in vs),5) for i in ab];hi=[round(max(v[i] for v in vs),5) for i in ab]
            planes[(axis,round(vs[0][axis],5))].append((lo,hi,p.material,1 if normals[0][axis]>0 else -1))
    build.PARTS.clear();parts={}
    def get(material):
        if material not in parts:
            name=build.MATERIALS[material][0]
            parts[material]=build.Part('Schematic locomotive '+name,'Locomotive',name,name=='lamp')
        return parts[material]
    for (axis,position),rects in planes.items():
        coords=[sorted({r[e][i] for r in rects for e in (0,1)}) for i in range(2)]
        index=[{v:i for i,v in enumerate(c)} for c in coords]
        grids={s:np.full((len(coords[1])-1,len(coords[0])-1),-1,dtype=np.int16) for s in (-1,1)}
        for lo,hi,material,s in rects:grids[s][index[1][lo[1]]:index[1][hi[1]],index[0][lo[0]]:index[0][hi[0]]]=material
        shared=(grids[1]>=0)&(grids[-1]>=0)
        for grid in grids.values():grid[shared]=-1
        ab=[i for i in range(3) if i!=axis]
        for sign,grid in grids.items():
            for y in range(grid.shape[0]):
                x=0
                while x<grid.shape[1]:
                    material=int(grid[y,x])
                    if material<0:x+=1;continue
                    xe=x+1
                    while xe<grid.shape[1] and grid[y,xe]==material:xe+=1
                    ye=y+1
                    while ye<grid.shape[0] and np.all(grid[ye,x:xe]==material):ye+=1
                    grid[y:ye,x:xe]=-1;p=get(material)
                    vs=[]
                    for u,v in [(coords[0][x],coords[1][y]),(coords[0][xe],coords[1][y]),(coords[0][xe],coords[1][ye]),(coords[0][x],coords[1][ye])]:
                        q=[0,0,0];q[axis]=position;q[ab[0]]=u;q[ab[1]]=v;vs.append(q)
                    outward=[0,0,0];outward[axis]=sign;p.face([p.vertex(v) for v in vs],outward);x=xe
    for old,vs,normals in other:
        p=get(old.material);p.face([p.vertex(v) for v in vs],normals=normals)
    return {'before':before,'after':sum(len(p.faces) for p in build.PARTS)}
