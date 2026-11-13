#version 150
uniform vec4 ColorModulator;
uniform float TrailTime;
uniform float EmissionPass;
in vec4 vertexColor;
in vec2 materialCoord;
out vec4 fragColor;

float hash(vec2 p) { return fract(sin(dot(p,vec2(127.1,311.7))) * 43758.5453); }
float noise(vec2 p) {
    vec2 cell=floor(p),f=fract(p);f=f*f*(3.0-2.0*f);
    return mix(mix(hash(cell),hash(cell+vec2(1,0)),f.x),
               mix(hash(cell+vec2(0,1)),hash(cell+vec2(1,1)),f.x),f.y);
}
void main() {
    bool rift=materialCoord.y>1.5;
    float age=clamp(materialCoord.x,0.0,1.0);
    float across=materialCoord.y-(rift?2.0:0.0);
    float flow=noise(vec2(age*7.0-TrailTime*1.8,across*5.0+TrailTime*0.55));
    float filaments=pow(0.5+0.5*sin(across*35.0+flow*6.0-age*13.0+TrailTime*5.0),5.0);
    float blade=exp(-pow((across-0.94)/0.055,2.0));
    float body=smoothstep(-0.04,0.20,across)*(1.0-smoothstep(0.86,1.04,across));
    float halo=exp(-pow((across-0.94)/0.18,2.0))*0.18;
    float dissolve=smoothstep(age*0.72-0.22,age*0.72+0.16,flow+(1.0-age)*0.28);
    float tail=1.0-smoothstep(0.55,1.0,age);
    float split=rift?0.40+0.60*smoothstep(0.04,0.15,abs(across-0.47+sin(age*19.0-TrailTime*4.0)*0.06)):1.0;
    float opacity=(body*(0.30+0.25*filaments)*dissolve*split+blade*0.84+halo)*tail;
    vec4 base=vertexColor*ColorModulator;
    vec3 bright=mix(base.rgb,vec3(0.91,0.99,1.0),0.72*blade+0.12*filaments);
    if(EmissionPass>0.5) {
        bright*=1.5+blade*4.0+filaments*1.2;
        opacity*=0.35+0.65*max(blade,filaments);
    }
    fragColor=vec4(bright,min(0.92,base.a*opacity));
}
