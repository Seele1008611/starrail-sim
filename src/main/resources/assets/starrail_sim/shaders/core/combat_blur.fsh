#version 150
uniform sampler2D SourceSampler;
uniform vec2 TexelStep;
uniform float BlurRadius;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec3 sum = vec3(0.0);
    float total = 0.0;
    for (int i = -6; i <= 6; ++i) {
        if (abs(float(i)) > BlurRadius) continue;
        float weight = exp(-float(i * i) / 12.0);
        sum += texture(SourceSampler, texCoord + TexelStep * float(i)).rgb * weight;
        total += weight;
    }
    fragColor = vec4(sum / total, 0.0);
}
