#version 150

uniform vec2 Size;
uniform float Radius;
uniform float ArcStart;
uniform float ArcLength;
uniform float BorderWidth;
uniform vec4 FillColor;
uniform vec4 BorderColor;
in vec2 localPosition;
out vec4 fragColor;

void main() {
    vec2 q = abs(localPosition - Size * 0.5) - (Size * 0.5 - vec2(Radius));
    float distance = length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0) - Radius;
    // Derivatives keep the transition approximately one framebuffer pixel wide.
    float aa = max(fwidth(distance) * 0.5, 0.0001);
    float coverage = 1.0 - smoothstep(-aa, aa, distance);
    float stroke = BorderWidth > 0.0
        ? smoothstep(-BorderWidth - aa, -BorderWidth + aa, distance) : 0.0;
    // Blend premultiplied colors, then return straight alpha for the GUI blend mode.
    float alpha = mix(FillColor.a, BorderColor.a, stroke);
    vec3 rgb = mix(FillColor.rgb * FillColor.a, BorderColor.rgb * BorderColor.a, stroke);
    fragColor = vec4(alpha > 0.0 ? rgb / alpha : vec3(0.0), alpha * coverage);
    if (ArcLength < 6.2831853) {
        vec2 p = localPosition - Size * 0.5;
        float angle = atan(p.y, p.x);
        float relative = mod(angle - ArcStart - ArcLength * 0.5 + 3.14159265, 6.2831853) - 3.14159265;
        float capDistance = (abs(relative) - ArcLength * 0.5) * max(length(p), 1.0);
        float capAA = max(fwidth(capDistance) * 0.5, 0.0001);
        fragColor.a *= 1.0 - smoothstep(-capAA, capAA, capDistance);
    }
    if (fragColor.a < 0.001) discard;
}
