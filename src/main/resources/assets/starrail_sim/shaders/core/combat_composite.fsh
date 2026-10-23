#version 150
uniform sampler2D SceneSampler;
uniform sampler2D BloomSampler;
uniform sampler2D DepthSampler;
uniform vec2 ScreenSize;
uniform float Strength;
uniform vec4 Impact0, Impact1, Impact2, Impact3;
uniform vec2 ImpactShape0, ImpactShape1, ImpactShape2, ImpactShape3;
uniform vec4 ImpactColor0, ImpactColor1, ImpactColor2, ImpactColor3;
in vec2 texCoord;
out vec4 fragColor;

void shock(vec4 impact, vec2 shape, vec4 tint, inout vec2 offset, inout vec2 chromatic,
           inout vec3 light, inout vec3 colourWash, inout float pixelation,
           inout vec2 pixelCenter, inout float pixelRadius) {
    if (impact.w <= 0.0 || shape.x <= 0.0) return;
    // Reject an obscured origin and foreground pixels; the depth is the captured world depth.
    float centerDepth = texture(DepthSampler, impact.xy).r;
    float pixelDepth = texture(DepthSampler, texCoord).r;
    if (centerDepth + 0.00001 < shape.y || pixelDepth + 0.00001 < shape.y) return;
    float aspect = ScreenSize.x / ScreenSize.y;
    vec2 delta = (texCoord - impact.xy) * vec2(aspect, 1.0);
    float distance = length(delta);
    float phase = clamp(impact.z, 0.0, 1.0);
    float envelope = sin(phase * 3.14159265) * (1.0 - phase);
    float style = tint.a;
    float angle = atan(delta.y, delta.x);
    float facets = style > 0.5 && style < 1.5 ? 1.0 + 0.055 * cos(6.0 * angle + phase * 0.7)
        : style > 1.5 ? 1.0 + 0.018 * sin(3.0 * angle) : 1.0;
    bool collapse = style > 4.5 && style < 5.05;
    float travel = collapse ? 2.8 - phase * 2.65 : 0.15 + phase * 2.65;
    float radius = shape.x * travel * facets;
    float width = max(0.004, shape.x * (style > 6.5 && style < 7.5 ? 0.11
        : style > 1.5 && style < 3.0 ? 0.22 : style > 0.5 ? 0.16 : 0.14));
    float ring = exp(-pow((distance - radius) / width, 2.0));
    vec2 direction = delta / max(distance, 0.0001) / vec2(aspect, 1.0);
    float weight = impact.w * envelope * Strength;
    float displacement = style > 7.5 ? 0.020 : style > 4.5 && style < 5.5 ? 0.027
        : style > 2.5 && style < 3.5 ? 0.023 : style > 1.5 && style < 3.0 ? 0.010
        : style > 0.5 ? 0.018 : 0.023;
    float orientation = collapse ? -1.0 : 1.0;
    float erosion = style > 4.5 && style < 5.5 ? 0.72 + 0.28 * sin(angle * 17.0 + phase * 18.0) : 1.0;
    offset += direction * ring * weight * displacement * orientation * erosion;
    if (style > 2.5 && style < 3.5) {
        vec2 tangent = vec2(-direction.y, direction.x);
        float heat = sin(angle * 11.0 + phase * 25.0 + sin(distance * 90.0));
        offset += tangent * ring * heat * weight * 0.004;
    } else if (style > 3.5 && style < 4.5) {
        // A narrow scan sheet sweeps through the target-centered calculation ring.
        float scan = exp(-pow((delta.y - (phase - 0.5) * shape.x * 4.0) / max(width * 0.45, 0.003), 2.0));
        offset.y += scan * weight * 0.004;
        light += tint.rgb * scan * weight * 0.12;
    } else if (style > 4.5 && style < 5.5) {
        light += tint.rgb * ring * weight * (0.08 + 0.06 * erosion);
    } else if (style > 6.5 && style < 7.5) {
        if (weight > pixelation) {
            pixelation = weight;
            pixelCenter = impact.xy;
            pixelRadius = shape.x * 3.2;
        }
    } else if (style > 7.5) {
        chromatic += direction * ring * weight * 0.008;
    }
    float flash = exp(-distance * distance / max(shape.x * shape.x * 4.0, 0.001));
    float ringLight = style > 6.5 && style < 7.5 ? 0.55 : style > 1.5 && style < 2.5 ? 0.25 : 0.42;
    light += tint.rgb * weight * (ring * ringLight + flash * (style > 1.5 ? 0.12 : 0.09));
    float wash = style > 5.5 && style < 6.5 ? 0.055 : style > 1.5 && style < 2.5 ? 0.065
        : style > 0.5 ? 0.035 : 0.012;
    colourWash += tint.rgb * flash * weight * wash;
}

void main() {
    vec2 offset = vec2(0.0);
    vec2 chromatic = vec2(0.0);
    vec3 light = vec3(0.0);
    vec3 colourWash = vec3(0.0);
    float pixelation = 0.0, pixelRadius = 0.0;
    vec2 pixelCenter = vec2(0.0);
    shock(Impact0, ImpactShape0, ImpactColor0, offset, chromatic, light, colourWash, pixelation, pixelCenter, pixelRadius);
    shock(Impact1, ImpactShape1, ImpactColor1, offset, chromatic, light, colourWash, pixelation, pixelCenter, pixelRadius);
    shock(Impact2, ImpactShape2, ImpactColor2, offset, chromatic, light, colourWash, pixelation, pixelCenter, pixelRadius);
    shock(Impact3, ImpactShape3, ImpactColor3, offset, chromatic, light, colourWash, pixelation, pixelCenter, pixelRadius);
    offset = clamp(offset, vec2(-0.035), vec2(0.035));
    vec2 margin = 0.5 / ScreenSize;
    vec2 uv = clamp(texCoord + offset, margin, 1.0 - margin);
    vec4 scene = texture(SceneSampler, uv);
    scene.r = texture(SceneSampler, clamp(uv + offset * 0.10 + chromatic, margin, 1.0 - margin)).r;
    scene.b = texture(SceneSampler, clamp(uv - offset * 0.10 - chromatic, margin, 1.0 - margin)).b;
    if (pixelation > 0.0 && pixelRadius > 0.0) {
        float cellPixels = mix(1.0, 13.0, clamp(pixelation, 0.0, 1.0));
        vec2 cell = vec2(cellPixels) / ScreenSize;
        vec2 coarseUV = pixelCenter + floor((uv - pixelCenter) / cell + 0.5) * cell;
        float aspect = ScreenSize.x / ScreenSize.y;
        float d = length((uv - pixelCenter) * vec2(aspect, 1.0));
        float mask = (1.0 - smoothstep(pixelRadius * 0.58, pixelRadius, d)) * pixelation;
        scene.rgb = mix(scene.rgb, texture(SceneSampler, clamp(coarseUV, margin, 1.0 - margin)).rgb, mask * 0.82);
    }
    vec3 bloom = texture(BloomSampler, texCoord).rgb;
    // Tone-map only the emission, leaving the world's colour and exposure unchanged when idle.
    bloom = vec3(1.0) - exp(-bloom * 1.6 * Strength);
    fragColor = vec4(scene.rgb + colourWash + bloom * 0.88 + min(light, vec3(0.35)), scene.a);
}
