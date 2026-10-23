#version 150

uniform vec4 ColorModulator;
uniform float CombatTime;
uniform float EmissionPass;

in vec4 vertexColor;
in vec2 materialCoord;
out vec4 fragColor;

void main() {
    vec4 color = vertexColor * ColorModulator;
    // UV0.y has a four-unit band per material and a 0..1 transverse coordinate.
    // All vertices of a face occupy the same band; no extra draw calls or textures.
    float material = floor(materialCoord.y * 0.25);
    if (material < 0.5) {
        fragColor = color;
        return;
    }
    float across = materialCoord.y - material * 4.0;
    float edge = 1.0 - smoothstep(0.035, 0.24, min(across, 1.0 - across));
    float along = materialCoord.x;
    if (material < 1.5) {
        float flow = smoothstep(0.48, 0.98, sin(along * 7.0 + CombatTime * 12.0));
        color.rgb = mix(color.rgb * (0.88 + 0.12 * flow), vec3(0.66, 0.92, 1.0), 0.20 * flow + 0.08 * edge);
        color.a *= 0.90 + 0.10 * max(flow, edge);
    } else if (material < 2.5) {
        float pulse = 0.5 + 0.5 * sin(along * 5.0 - CombatTime * 3.4);
        float bands = smoothstep(0.78, 0.97, sin(along * 15.0 - CombatTime * 4.0));
        color.rgb = mix(color.rgb * (0.88 + 0.12 * pulse), vec3(0.96, 0.76, 0.36), 0.18 * edge + 0.10 * bands);
        color.a *= 0.90 + 0.10 * pulse;
    } else if (material < 3.5) {
        float sap = smoothstep(0.35, 0.98, sin(along * 8.0 - CombatTime * 3.5));
        float vein = 1.0 - smoothstep(0.03, 0.16, abs(across - 0.5));
        color.rgb = mix(color.rgb * (0.90 + 0.10 * sap), vec3(0.84, 0.92, 0.64), 0.16 * sap * vein + 0.05 * edge);
        color.a *= 0.92 + 0.08 * sap;
    } else if (material < 4.5) {
        float heat = 0.5 + 0.5 * sin(along * 11.0 + CombatTime * 9.0 + sin(across * 18.0));
        color.rgb = mix(color.rgb * (0.88 + 0.12 * heat), vec3(1.0, 0.48, 0.20), 0.18 * heat + 0.12 * edge);
        color.a *= 0.90 + 0.10 * heat;
    } else if (material < 5.5) {
        float scan = smoothstep(0.82, 0.98, sin(along * 18.0 - CombatTime * 8.0));
        float calculation = 0.5 + 0.5 * sin(along * 6.0 + CombatTime * 2.5);
        color.rgb = mix(color.rgb * (0.87 + 0.13 * calculation), vec3(0.86, 0.62, 1.0), 0.16 + 0.26 * scan + 0.08 * edge);
        color.a *= 0.88 + 0.12 * max(scan, calculation);
    } else if (material < 6.5) {
        float erosion = smoothstep(0.28, 0.98, sin(along * 9.0 - CombatTime * 5.0 + sin(across * 24.0)));
        float rim = 1.0 - smoothstep(0.01, 0.22, abs(across - 0.5));
        color.rgb = mix(color.rgb * (0.78 + 0.22 * erosion), vec3(0.48, 0.24, 0.82), 0.14 + 0.16 * rim);
        color.a *= 0.90 + 0.10 * erosion;
    } else if (material < 7.5) {
        float beat = 0.5 + 0.5 * sin(along * 4.5 - CombatTime * 3.14159);
        float note = smoothstep(0.86, 0.99, sin(along * 13.0 - CombatTime * 6.28318));
        color.rgb = mix(color.rgb * (0.91 + 0.09 * beat), vec3(1.0, 0.72, 0.88), 0.18 * beat + 0.12 * note);
        color.a *= 0.92 + 0.08 * beat;
    } else if (material < 8.5) {
        float frost = 0.5 + 0.5 * sin(along * 8.0 - CombatTime * 2.2);
        float shard = smoothstep(0.84, 0.98, sin(along * 22.0 + across * 17.0));
        color.rgb = mix(color.rgb * (0.88 + 0.12 * frost), vec3(0.60, 0.91, 1.0), 0.18 * frost + 0.25 * shard);
        color.a *= 0.90 + 0.10 * frost;
    } else {
        float bounce = 0.5 + 0.5 * sin(along * 9.0 + CombatTime * 7.0);
        float confetti = smoothstep(0.72, 0.98, sin(along * 17.0 + across * 21.0 + CombatTime * 5.0));
        vec3 party = mix(vec3(1.0, 0.34, 0.66), vec3(1.0, 0.83, 0.35), confetti);
        color.rgb = mix(color.rgb * (0.85 + 0.15 * bounce), party, 0.18 * bounce + 0.20 * confetti);
        color.a *= 0.88 + 0.12 * max(bounce, confetti);
    }
    // A separate HDR target captures emission without brightening the whole world.
    if (EmissionPass > 0.5) {
        if (material < 0.5 || material > 9.5) discard;
        float energy;
        if (material < 1.5) {
            energy = 3.4 + 1.8 * smoothstep(0.0, 1.0, sin(along * 9.0 + CombatTime * 14.0));
        } else if (material < 2.5) {
            float shieldPulse = 0.5 + 0.5 * sin(along * 10.0 - CombatTime * 5.5);
            color.rgb = mix(color.rgb, vec3(1.0, 0.72, 0.28), 0.16 + 0.14 * edge);
            energy = 2.8 + 1.5 * shieldPulse + 0.8 * edge;
        } else if (material < 3.5) {
            float lifePulse = 0.5 + 0.5 * sin(along * 7.0 - CombatTime * 3.2);
            color.rgb = mix(color.rgb, vec3(0.48, 1.0, 0.66), 0.14 + 0.10 * edge);
            energy = 2.25 + 1.15 * lifePulse + 0.55 * edge;
        } else if (material < 4.5) {
            float ember = 0.5 + 0.5 * sin(along * 13.0 + CombatTime * 11.0);
            color.rgb = mix(color.rgb, vec3(1.0, 0.34, 0.10), 0.22 + 0.16 * edge);
            energy = 3.0 + 1.4 * ember + 0.7 * edge;
        } else if (material < 5.5) {
            float solve = 0.5 + 0.5 * sin(along * 10.0 - CombatTime * 5.0);
            color.rgb = mix(color.rgb, vec3(0.74, 0.46, 1.0), 0.18 + 0.16 * edge);
            energy = 2.55 + 1.5 * solve + 0.65 * edge;
        } else if (material < 6.5) {
            float voidPulse = 0.5 + 0.5 * sin(along * 8.0 - CombatTime * 4.0);
            color.rgb = mix(color.rgb, vec3(0.62, 0.30, 1.0), 0.22 + 0.16 * edge);
            energy = 2.35 + 1.25 * voidPulse + 0.75 * edge;
        } else if (material < 7.5) {
            float chord = 0.5 + 0.5 * sin(along * 6.0 - CombatTime * 6.28318);
            color.rgb = mix(color.rgb, vec3(1.0, 0.56, 0.82), 0.18 + 0.16 * edge);
            energy = 2.45 + 1.2 * chord + 0.55 * edge;
        } else if (material < 8.5) {
            float ice = 0.5 + 0.5 * sin(along * 9.0 - CombatTime * 2.6);
            color.rgb = mix(color.rgb, vec3(0.48, 0.88, 1.0), 0.2 + 0.15 * edge);
            energy = 2.8 + 1.35 * ice + 0.8 * edge;
        } else {
            float burst = 0.5 + 0.5 * sin(along * 11.0 + CombatTime * 8.0);
            color.rgb = mix(color.rgb, vec3(1.0, 0.48, 0.76), 0.2 + 0.16 * edge);
            energy = 2.8 + 1.6 * burst + 0.65 * edge;
        }
        color.rgb *= energy;
    }
    fragColor = color;
}
